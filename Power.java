import java.nio.file.Path;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.Planetiler;
import com.onthegomap.planetiler.Profile;
import com.onthegomap.planetiler.config.Arguments;
import com.onthegomap.planetiler.reader.SourceFeature;

/**
 * Generates vector tiles with power lines from OpenStreetMap (see
 * https://wiki.openstreetmap.org/wiki/Tag:power%3Dline)
 * 
 * To run:
 * - java -cp planetiler.jar Power.java
 * - open power.pmtiles in https://pmtiles.io
 */
public class Power implements Profile {

  public static void main(String[] args) {
    var arguments = Arguments.fromArgs(args).withDefault("download", true);
    String area = arguments.getString("area", "geofabrik area to download", "rhode-island");
    Planetiler.create(arguments)
        .addOsmSource("osm", Path.of("data", area + ".osm.pbf"), "geofabrik:" + area)
        .overwriteOutput(Path.of("power.pmtiles"))
        .setProfile(new Power())
        .run();
  }

  @Override
  public void processFeature(SourceFeature sourceFeature, FeatureCollector features) {
    if (sourceFeature.canBeLine() && sourceFeature.hasTag("power", "line")) {
      features.line("power")
          .inheritAttrFromSource("voltage");
    }
  }

  @Override
  public String attribution() {
    return OSM_ATTRIBUTION;
  }

  @Override
  public boolean isOverlay() {
    return true;
  }
}