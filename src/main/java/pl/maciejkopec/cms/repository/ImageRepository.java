package pl.maciejkopec.cms.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pl.maciejkopec.cms.domain.ImageDocument;

public interface ImageRepository extends ReactiveMongoRepository<ImageDocument, String> {}
