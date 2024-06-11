package overturelayers;

import com.onthegomap.planetiler.ForwardingProfile.HandlerForLayer;
import com.onthegomap.planetiler.expression.Expression;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.reader.Struct;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.onthegomap.planetiler.ForwardingProfile.FeatureProcessor;

/** Base layer that all overture layer handlers extend. */
abstract class BaseLayer implements HandlerForLayer, FeatureProcessor {
  final String LAYER;
  private final Expression filter;

  BaseLayer(String layer, List<String> types) {
    this.LAYER = layer;
    this.filter = Expression.matchAny("type", types);
  }

  @Override
  public String name() {
    return LAYER;
  }

  @Override
  public Expression filter() {
    return filter;
  }

  static Map<String, Object> names(SourceFeature feature) {
    return names(feature.getStruct("names"));
  }

  static Map<String, Object> names(Struct names) {
    if (names.isNull()) {
      return Map.of();
    }
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("name", names.get("primary").asString());
    for (var entry : names.get("common").asMap().entrySet()) {
      result.put("name:" + entry.getKey(), entry.getValue().asString());
    }
    return result;
  }
}
