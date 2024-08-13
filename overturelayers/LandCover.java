package overturelayers;

import java.util.List;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile.LayerPostProcesser;
import com.onthegomap.planetiler.VectorTile.Feature;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.config.PlanetilerConfig;
import com.onthegomap.planetiler.reader.SourceFeature;

public class LandCover extends BaseLayer implements LayerPostProcesser {

  private PlanetilerConfig config;

  public LandCover(PlanetilerConfig config) {
    super("land_cover", List.of("land_cover"));
    this.config = config;
  }

  @Override
  public void processFeature(SourceFeature sourceFeature, FeatureCollector features) {
    var cartography = sourceFeature.getStruct("cartography");
    var minZoom = cartography.get("min_zoom").asInt();
    var maxZoom = cartography.get("max_zoom").asInt();

    features.polygon("land_cover")
        .setMinZoom(minZoom)
        .setMaxZoom(maxZoom)
        .inheritAttrsFromSource("subtype");
  }

  @Override
  public List<Feature> postProcess(int zoom, List<Feature> items) throws GeometryException {
    double minSize = zoom < 13 ? 8 : config.minFeatureSize(zoom);
    items = FeatureMerge.mergeOverlappingPolygons(items, minSize);
    return items;
  }
}
