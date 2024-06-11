import java.nio.file.Path;
import java.util.List;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.Planetiler;
import com.onthegomap.planetiler.Profile;
import com.onthegomap.planetiler.config.Arguments;
import com.onthegomap.planetiler.reader.SourceFeature;

/**
 * A single-file profile that generates vector tiles from Overture Maps building
 * data (https://docs.overturemaps.org/schema/reference/buildings/building/)
 * 
 * To run:
 * - download just overture building data in a bounding box to
 * data/buildings.parquet (https://docs.overturemaps.org/getting-data/)
 * - Run java -cp planetiler.jar OvertureBuildings.java
 * - Open data/overture-buildings.pmtiles in https://pmtiles.io/
 */
public class OvertureBuildings implements Profile {
  public static void main(String[] args) {
    var arguments = Arguments.fromArgsOrConfigFile(args);
    Planetiler.create(arguments)
        .setProfile(new OvertureBuildings())
        .addParquetSource("overture", List.of(Path.of("data", "buildings.parquet")))
        .overwriteOutput(Path.of("data", "overture-buildings.pmtiles"))
        .run();
  }

  @Override
  public void processFeature(SourceFeature source, FeatureCollector features) {
    features.polygon("building")
        .setMinZoom(14)
        .inheritAttrsFromSource(
            "subtype",
            "class",
            "level",
            "height",
            "num_floors",
            "min_height",
            "min_floor");
  }

  @Override
  public boolean isOverlay() {
    return true;
  }

  @Override
  public String attribution() {
    return """
        <a href="https://www.openstreetmap.org/copyright" target="_blank">&copy; OpenStreetMap</a>
        <a href="https://docs.overturemaps.org/attribution" target="_blank">&copy; Overture Maps Foundation</a>
        """
        .replace("\n", " ")
        .trim();
  }
}
