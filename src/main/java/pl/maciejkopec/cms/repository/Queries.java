package pl.maciejkopec.cms.repository;

import jakarta.validation.constraints.NotNull;
import lombok.experimental.UtilityClass;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@UtilityClass
public final class Queries {

  @NotNull
  public static Query byId(final String id) {
    return Query.query(Criteria.where("_id").is(id));
  }
}
