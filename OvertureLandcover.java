import java.nio.file.Path;
import java.util.List;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.Planetiler;
import com.onthegomap.planetiler.Profile;
import com.onthegomap.planetiler.config.Arguments;
import com.onthegomap.planetiler.reader.SourceFeature;

/**
 * A single-file profile that generates vector tiles from Overture Maps Land Cover
 * data (https://docs.overturemaps.org/schema/reference/base/land_cover/)
 * 
 * To run:
 * - download just overture land cover data in a bounding box to
 * data/land_cover.parquet (https://docs.overturemaps.org/getting-data/)
 * - Run java -cp planetiler.jar OvertureLandcover.java
 * - Open data/overture-land_cover.pmtiles in https://pmtiles.io/
 */
public class OvertureLandcover implements Profile {
  public static void main(String[] args) {
    var arguments = Arguments.fromArgsOrConfigFile(args);
    Planetiler.create(arguments)
        .setProfile(new OvertureLandcover())
        .addParquetSource("overture", List.of(Path.of("data", "land_cover.parquet")))
        .overwriteOutput(Path.of("data", "overture-land_cover.pmtiles"))
        .run();
  }

  @Override
  public void processFeature(SourceFeature source, FeatureCollector features) {
    var cartography = source.getStruct("cartography");
    var minZoom = cartography.get("min_zoom").asInt();
    var maxZoom = cartography.get("max_zoom").asInt();

    features.polygon("land_cover")
        .setMinZoom(minZoom)
        .setMaxZoom(maxZoom)
        .inheritAttrsFromSource("subtype");
  }

  @Override
  public boolean isOverlay() {
    return true;
  }

  @Override
  public String attribution() {
    return """
        <a href="https://docs.overturemaps.org/attribution" target="_blank">&copy; Overture Maps Foundation</a>
        """
        .replace("\n", " ")
        .trim();
  }
}
