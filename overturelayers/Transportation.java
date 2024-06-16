package overturelayers;

import java.util.List;

import com.google.common.collect.Range;
import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile.LayerPostProcesser;
import com.onthegomap.planetiler.VectorTile.Feature;
import com.onthegomap.planetiler.config.PlanetilerConfig;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.reader.Struct;

/**
 * Overture data handler for transportation segments
 * https://docs.overturemaps.org/schema/reference/transportation/segment/
 * 
 * Run from Overture.java in the root
 */
public class Transportation extends BaseLayer implements LayerPostProcesser {

  /*
   * road.restrictions.speed_limits speed_limits
   * road.restrictions.access access_restrictions
   * road.restrictions.prohibited_transitions prohibited_transitions
   * road.surface road_surface
   * road.flags road_flags
   * road.width width_rules
   * road.lanes lanes
   * road.level level_rules
   */

  private PlanetilerConfig config;

  public Transportation(PlanetilerConfig config) {
    // output layer='transportation' and filter to input features where
    // type='segment'
    super("transportation", List.of("segment"));
    this.config = config;
  }

  @Override
  public void processFeature(SourceFeature sourceFeature, FeatureCollector features) {
    int minzoom = switch (sourceFeature.getString("class")) {
      case "motorway" -> 4;
      case "trunk" -> 6;
      case "primary" -> 7;
      case "secondary" -> 9;
      case "tertiary" -> 11;
      case "minor" -> 13;
      case "path", "footway", "track", "cycleway", "bridleway" -> 13;
      case null, default -> 14;
    };

    var feature = features.line(LAYER)
        .setMinZoom(minzoom)
        .setMinPixelSize(0)
        .inheritAttrsFromSource("subtype", "class");

    var names = sourceFeature.getStruct("names");
    if (!names.isNull()) {
      String primary = names.get("primary").asString();
      double minSize = 10;
      feature.setAttrWithMinSize("name", primary, minSize);
      for (var variant : names.get("common").asMap().entrySet()) {
        feature.setAttrWithMinSize("name:" + variant.getKey(),
            variant.getValue().rawValue(), minSize);
      }
      for (var rule : names.get("rules").asList()) {
        var partial = feature.linearRange(range(rule));
        var language = rule.get("language").asString();
        String key = language == null ? "name" : ("name:" + language);
        partial.setAttrWithMinSize(key, rule.get("value").asString(), minSize);
      }
    }

    for (var surface : sourceFeature.getStruct("road_surface").asList()) {
      feature.linearRange(range(surface)).setAttrWithMinzoom("surface", surface.get("value").asString(), 12);
    }

    for (var flags : sourceFeature.getStruct("road_flags").asList()) {
      var range = feature.linearRange(range(flags));
      for (var value : flags.get("values").asList()) {
        var str = value.asString();
        range.setAttrWithMinSize(str, true, 4, 4, 12);
        if ("is_link".equals(str)) {
          range.setMinZoom(Math.max(minzoom, 9));
        }
      }
    }

    for (var level : sourceFeature.getStruct("level_rules").asList()) {
      feature.linearRange(range(level)).setAttrWithMinSize("level", level.get("value").asInt(), 4, 9, 12);
    }

    for (var width : sourceFeature.getStruct("width_rules").asList()) {
      feature.linearRange(range(width)).setAttrWithMinzoom("width", width.get("value").asDouble(), 12);
    }

    for (var lanes : sourceFeature.getStruct("lanes").asList()) {
      var value = lanes.get("value").asJson();
      if (!"null".equals(value)) {
        feature.linearRange(range(lanes)).setAttrWithMinzoom("lanes", value, 12);
      }
    }

    for (var level : sourceFeature.getStruct("level_rules").asList()) {
      var value = level.get("value").asInt();
      if (value != null) {
        feature.linearRange(range(level)).setAttrWithMinzoom("level", value, 12);
      }
    }

    for (var limit : sourceFeature.getStruct("speed_limits").asList()) {
      var range = feature.linearRange(range(limit));
      var min = limit.get("min_speed");
      var max = limit.get("max_speed");
      var when = limit.get("when");
      var whenString = when.isNull() ? null : when.asJson();
      if (!min.isNull()) {
        range.setAttrWithMinzoom("min_speed", min.get("value").asString() + min.get("unit").asString(), 12);
        range.setAttrWithMinzoom("min_speed_when", whenString, 12);
      }
      if (!max.isNull()) {
        range.setAttrWithMinzoom("max_speed", max.get("value").asString() + max.get("unit").asString(), 12);
        range.setAttrWithMinzoom("max_speed_when", whenString, 12);
      }
    }

    for (var access : sourceFeature.getStruct("access_restrictions").asList()) {
      var range = feature.linearRange(range(access));
      var type = access.get("access_type");
      var when = access.get("when");
      var whenString = when.isNull() ? null : when.asJson();
      if (!type.isNull()) {
        range.setAttrWithMinzoom("access", type.asString(), 12);
        range.setAttrWithMinzoom("access_when", whenString, 12);
      }
    }
  }

  Range<Double> FULL_RANGE = Range.closedOpen(0d, 1d);

  Range<Double> range(Struct struct) {
    Struct between = struct.get("between");
    if (between.isNull()) {
      return FULL_RANGE;
    }
    Double start = between.get(0).asDouble();
    Double end = between.get(1).asDouble();
    return Range.closedOpen(start == null ? 0 : start, end == null ? 1 : end);
  }

  @Override
  public List<Feature> postProcess(int zoom, List<Feature> items) throws GeometryException {
    double minSize = config.minFeatureSize(zoom);
    double tolerance = config.tolerance(zoom);
    items = FeatureMerge.mergeLineStrings(items, minSize, tolerance, 4);
    return items;
  }

}
