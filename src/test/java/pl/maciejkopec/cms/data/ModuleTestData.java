package pl.maciejkopec.cms.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.maciejkopec.cms.domain.ModuleDocument;
import pl.maciejkopec.cms.domain.ModuleType;
import pl.maciejkopec.cms.dto.Module;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ModuleTestData {

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Dto {

    public static Module minimum() {
      return Module.builder().id("id").type(ModuleType.ICONS).order(0).build();
    }

    public static Module valid() {
      return minimum().toBuilder().title("Title of module").data("{}").build();
    }

    public static Module notSaved() {
      return valid().toBuilder().id(null).build();
    }
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Document {

    public static ModuleDocument minimum() {
      return ModuleDocument.builder().id("id").type(ModuleType.ICONS).order(0).build();
    }

    public static ModuleDocument valid() {
      return minimum().toBuilder().title("Title of module").data("{}").build();
    }

    public static ModuleDocument notSaved() {
      return valid().toBuilder().id(null).build();
    }
  }
}
