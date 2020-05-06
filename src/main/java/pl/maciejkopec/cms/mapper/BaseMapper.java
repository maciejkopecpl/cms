package pl.maciejkopec.cms.mapper;

public interface BaseMapper<D, E> {

  E toDomain(D d);

  D toDto(E e);
}
