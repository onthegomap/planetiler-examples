import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.Planetiler;
import com.onthegomap.planetiler.Profile;
import com.onthegomap.planetiler.config.Arguments;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.reader.osm.OsmElement;
import com.onthegomap.planetiler.reader.osm.OsmSourceFeature;
import java.nio.file.Path;

/**
 * Generates tiles with a raw copy of OSM data in a single "osm" layer at one
 * zoom level, similar to http://osmlab.github.io/osm-qa-tiles/.
 * 
 * Nodes are mapped to points and ways are mapped to polygons or linestrings,
 * and multipolygon relations are mapped to polygons. Each output feature
 * contains all key/value tags from the input feature, plus these extra
 * attributes:
 * 
 * - @type: node, way, or relation
 * - @id: OSM element ID
 * - @changeset: Changeset that last modified the element
 * - @timestamp: Timestamp at which the element was last modified
 * - @version: Version number of the OSM element
 * - @uid: User ID that last modified the element
 * - @user: User name that last modified the element
 * 
 * To run:
 * - java -cp planetiler.jar OsmQaTiles.java --area="rhode island"
 * - open data/qa.pmtiles in https://pmtiles.io/#map=13.03/41.67807/-71.60054
 */
public class OsmQaTiles implements Profile {

  public static void main(String[] args) {
    Arguments arguments = Arguments.fromArgsOrConfigFile(args);
    int zoom = arguments.getInteger("zoom", "zoom level to generate tiles at", 12);
    String area = arguments.getString("area", "geofabrik area to download", "monaco");
    var args2 = arguments
        .withDefault("minzoom", zoom)
        .withDefault("maxzoom", zoom)
        .withDefault("tile_warning_size_mb", 100)
        .withDefault("download", true);
    Planetiler.create(args2)
        .setProfile(new OsmQaTiles())
        .addOsmSource("osm",
            Path.of("data", area + ".osm.pbf"),
            "planet".equalsIgnoreCase(area) ? "aws:latest" : ("geofabrik:" + area))
        .overwriteOutput(Path.of("data", "qa.pmtiles"))
        .run();
  }

  @Override
  public void processFeature(SourceFeature sourceFeature, FeatureCollector features) {
    if (!sourceFeature.tags().isEmpty() && sourceFeature instanceof OsmSourceFeature osmFeature) {
      var element = osmFeature.originalElement();
      var feature = features.anyGeometry("osm")
          .setMinPixelSize(0)
          .setPixelTolerance(0)
          .setBufferPixels(0)
          .putAttrs(sourceFeature.tags())
          .setAttr("@id", sourceFeature.id())
          .setAttr("@type", switch (element) {
            case OsmElement.Node ignored -> "node";
            case OsmElement.Way ignored -> "way";
            case OsmElement.Relation ignored -> "relation";
            default -> null;
          });
      var info = element.info();
      if (info != null) {
        feature
            .setAttr("@version", info.version() == 0 ? null : info.version())
            .setAttr("@timestamp", info.timestamp() == 0L ? null : info.timestamp())
            .setAttr("@changeset", info.changeset() == 0L ? null : info.changeset())
            .setAttr("@uid", info.userId() == 0 ? null : info.userId())
            .setAttr("@user", info.user() == null || info.user().isBlank() ? null : info.user());
      }
    }
  }

  @Override
  public String attribution() {
    return OSM_ATTRIBUTION;
  }
}