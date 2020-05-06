package pl.maciejkopec.cms.repository;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Queries {

  @NotNull
  public static Query byId(final String id) {
    return Query.query(Criteria.where("_id").is(id));
  }
}
