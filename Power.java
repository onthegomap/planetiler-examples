import java.nio.file.Path;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.Planetiler;
import com.onthegomap.planetiler.Profile;
import com.onthegomap.planetiler.config.Arguments;
import com.onthegomap.planetiler.reader.SourceFeature;

public class Power implements Profile {

  @Override
  public void processFeature(SourceFeature sourceFeature, FeatureCollector features) {
    if (sourceFeature.canBeLine() && sourceFeature.hasTag("power", "line")) {
      features.line("power")
          .inheritAttrFromSource("voltage");
    }
  }

  @Override
  public String name() {
    return "Power";
  }

  public static void main(String[] args) throws Exception {
    Planetiler.create(Arguments.fromArgs(args).orElse(Arguments.fromArgs("--download")))
        .addOsmSource("osm", Path.of("ri.osm.pbf"), "geofabrik:rhode-island")
        .overwriteOutput(Path.of("power.pmtiles"))
        .setProfile(new Power())
        .run();
  }
}