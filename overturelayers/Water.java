package overturelayers;

import java.util.List;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile.LayerPostProcesser;
import com.onthegomap.planetiler.VectorTile.Feature;
import com.onthegomap.planetiler.config.PlanetilerConfig;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;

/**
 * Overture data handler for water features
 * https://docs.overturemaps.org/schema/reference/base/water/
 * 
 * Run from Overture.java in the root
 */
public class Water extends BaseLayer implements LayerPostProcesser {

  private PlanetilerConfig config;

  public Water(PlanetilerConfig config) {
    // output layer='water' and filter to input features where type='water'
    super("water", List.of("water"));
    this.config = config;
  }

  @Override
  public void processFeature(SourceFeature sourceFeature, FeatureCollector features) {
    String clazz = sourceFeature.getString("class");
    int minzoom = switch (clazz) {
      case "lake", "ocean", "reservoir" -> 0;
      case "river" -> 9;
      case "canal" -> 12;
      case null, default -> 13;
    };
    if (sourceFeature.isPoint()) {
      minzoom = "ocean".equals(clazz) ? 0 : Math.max(8, minzoom);
    } else if (sourceFeature.canBePolygon()) {
      minzoom = Math.min(minzoom, 6);
    } else if (sourceFeature.canBeLine()) {
      minzoom = Math.max(9, minzoom);
    }

    var feature = features.anyGeometry(LAYER)
        .setMinZoom(minzoom)
        .setMinPixelSize(0)
        .inheritAttrsFromSource("subtype", "class");
    int minZoomForDetails = Math.clamp(feature.getMinZoomForPixelSize(8), 6, 13);
    feature
        .inheritAttrsFromSourceWithMinzoom(minZoomForDetails,
            "is_salt",
            "is_intermittent",
            "level");

    if (sourceFeature.isPoint() || sourceFeature.canBeLine()) {
      feature.putAttrsWithMinzoom(names(sourceFeature), minZoomForDetails);
    }
  }

  @Override
  public List<Feature> postProcess(int zoom, List<Feature> items) throws GeometryException {
    double minSize = zoom < 13 ? 8 : config.minFeatureSize(zoom);
    double tolerance = config.tolerance(zoom);
    items = FeatureMerge.mergeOverlappingPolygons(items, minSize);
    items = FeatureMerge.mergeLineStrings(items, minSize, tolerance, 4);
    return items;
  }
}
