package pl.maciejkopec.cms.dto;

import org.bson.types.ObjectId;

public record SavedImage(ObjectId objectId, String filename) {

}
