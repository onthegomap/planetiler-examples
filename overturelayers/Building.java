package overturelayers;

import java.util.List;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile.LayerPostProcesser;
import com.onthegomap.planetiler.VectorTile.Feature;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;

/**
 * Overture data handler for the buildings
 * https://docs.overturemaps.org/schema/reference/buildings/building/ and
 * building parts
 * https://docs.overturemaps.org/schema/reference/buildings/building_part/
 * 
 * Run from Overture.java in the root
 */
public class Building extends BaseLayer implements LayerPostProcesser {
  public Building() {
    // output layer='building' and filter to input features where type in
    // ('building', 'building_part')
    super("building", List.of("building", "building_part"));
  }

  @Override
  public void processFeature(SourceFeature sourceFeature, FeatureCollector features) {
    boolean isPart = sourceFeature.hasTag("type", "building_part");
    features.polygon(LAYER)
        .setMinZoom(isPart ? 14 : 13)
        .setMinPixelSize(2)
        .inheritAttrsFromSourceWithMinzoom(14,
            "subtype",
            "class",
            "level",
            "height",
            "num_floors",
            "min_height",
            "min_floor",
            "facade_color",
            "facade_material",
            "roof_material",
            "roof_shape",
            "roof_direction",
            "roof_orientation",
            "roof_color",
            "eave_height")
        .setSortKey((int) (sourceFeature.getStruct("height").orElse(0).asDouble() * 10))
        .setAttr("parts", isPart ? "is" : sourceFeature.getBoolean("has_parts") ? "has" : null)
        // TODO names here or as a point?
        .putAttrsWithMinzoom(names(sourceFeature), 14);
  }

  @Override
  public List<Feature> postProcess(int zoom, List<Feature> items) throws GeometryException {
    return FeatureMerge.mergeMultiPolygon(items);
  }
}
