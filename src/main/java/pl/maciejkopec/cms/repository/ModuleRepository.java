package pl.maciejkopec.cms.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pl.maciejkopec.cms.domain.ModuleDocument;

public interface ModuleRepository extends ReactiveMongoRepository<ModuleDocument, String> {}
