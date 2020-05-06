package pl.maciejkopec.cms.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.maciejkopec.cms.domain.ModuleDocument;
import pl.maciejkopec.cms.dto.Module;
import pl.maciejkopec.cms.dto.graphql.CreateModulePayload;
import pl.maciejkopec.cms.dto.graphql.UpdateModulePayload;

@Mapper
public interface ModuleMapper extends BaseMapper<Module, ModuleDocument> {
  @Override
  ModuleDocument toDomain(Module module);

  @Override
  Module toDto(ModuleDocument moduleDocument);

  @Mapping(target = "status", ignore = true)
  @Mapping(target = "module", source = ".")
  CreateModulePayload toCreatePayload(ModuleDocument module);

  @Mapping(target = "status", ignore = true)
  @Mapping(target = "module", source = ".")
  CreateModulePayload toCreatePayload(Module module);

  @Mapping(target = "status", ignore = true)
  @Mapping(target = "module", source = ".")
  UpdateModulePayload toUpdatePayload(ModuleDocument module);

  @Mapping(target = "status", ignore = true)
  @Mapping(target = "module", source = ".")
  UpdateModulePayload toUpdatePayload(Module module);
}
