import java.nio.file.Path;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.Planetiler;
import com.onthegomap.planetiler.config.Arguments;
import com.onthegomap.planetiler.config.PlanetilerConfig;
import com.onthegomap.planetiler.util.Glob;

import overturelayers.Building;
import overturelayers.LandCover;
import overturelayers.Transportation;
import overturelayers.Water;

/**
 * A more complex profile that generates vector tiles from Overture Maps data
 * (https://overturemaps.org/) where logic is broken-out into separate files in
 * overturelayers subdirectory.
 * 
 * To run:
 * - download a subset of overture data for a bounding box:
 * https://docs.overturemaps.org/getting-data/
 * - Run java -cp planetiler.jar Overture.java
 * - Open data/overture.pmtiles in https://pmtiles.io/
 */
public class Overture extends ForwardingProfile {

  Overture(PlanetilerConfig config) {
    super(config);
    registerHandler(new Water(config));
    registerHandler(new LandCover(config));
    registerHandler(new Transportation(config));
    registerHandler(new Building());
  }

  @Override
  public boolean isOverlay() {
    return true;
  }

  public static void main(String[] args) {
    var arguments = Arguments.fromArgsOrConfigFile(args);
    Path base = arguments.inputFile("base", "overture base directory", Path.of("data", "overture"));
    Planetiler.create(arguments)
        .setProfile(pt -> new Overture(pt.config()))
        .addParquetSource("overture",
            Glob.of(base).resolve("**", "*.parquet").find(),
            true, // hive-partitioning
            fields -> fields.get("id"), // hash the ID field to generate unique long IDs
            fields -> fields.get("type")) // extract "type={}" from the filename to get layer
        .overwriteOutput(Path.of("data", "overture.pmtiles"))
        .run();
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
