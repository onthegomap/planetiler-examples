import java.nio.file.Path;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.Planetiler;
import com.onthegomap.planetiler.Profile;
import com.onthegomap.planetiler.config.Arguments;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.util.ZoomFunction;

/**
 * Builds a map of toilets from OpenStreetMap nodes tagged with
 * https://wiki.openstreetmap.org/wiki/Tag:amenity%3Dtoilets.
 * 
 * To run this example:
 * - Run java -cp planetiler.jar Toilets.java
 * - Open data/toilets.pmtiles in https://pmtiles.io
 */
public class Toilets implements Profile {

  /*
   * Main entrypoint for the example program
   */
  public static void main(String[] args) throws Exception {
    var arguments = Arguments.fromArgsOrConfigFile(args).withDefault("download", true);
    String area = arguments.getString("area", "geofabrik area to download", "monaco");
    Planetiler.create(arguments)
        .setProfile(new Toilets())
        // override this default with --osm-path="path/to/data.osm.pbf"
        .addOsmSource("osm", Path.of("data", area + ".osm.pbf"), "geofabrik:" + area)
        // override this default with --output="path/to/output.pmtiles"
        .overwriteOutput(Path.of("data", "toilets.pmtiles"))
        .run();
  }

  @Override
  public void processFeature(SourceFeature sourceFeature, FeatureCollector features) {
    if (sourceFeature.isPoint() && sourceFeature.hasTag("amenity", "toilets")) {
      features.point("toilets")
          .setZoomRange(0, 14)
          // to limit toilets displayed at lower zoom levels:
          // divide each 256x256 px tile into 32x32 px squares
          // and in each square only include
          // the toilets with the lowest sort key within that square
          .setPointLabelGridSizeAndLimit(
              12, // only limit at z12 and below
              32, // break the tile up into 32x32 px squares
              4 // any only keep the 4 nodes with lowest sort-key in each 32px square
          )
          // and also whenever you set a label grid size limit, make sure you increase the
          // buffer size so no
          // label grid squares will be the consistent between adjacent tiles
          .setBufferPixelOverrides(ZoomFunction.maxZoom(12, 32));
    }
  }

  @Override
  public boolean isOverlay() {
    return true;
  }

  @Override
  public String attribution() {
    return OSM_ATTRIBUTION;
  }
}
