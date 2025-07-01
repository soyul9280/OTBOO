package com.codeit.weatherwear.domain.clothes.repository;

import com.codeit.weatherwear.domain.clothes.entity.Cloth;
import com.codeit.weatherwear.domain.clothes.entity.ClothType;
import com.codeit.weatherwear.domain.clothes.entity.QCloth;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@RequiredArgsConstructor
public class ClothRepositoryCustomImpl implements ClothRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Cloth> searchCloths(Instant cursor, UUID idAfter, int limit, ClothType typeEqual,
        UUID ownerId) {
        QCloth cloth = QCloth.cloth;

        BooleanBuilder condition = new BooleanBuilder();

        condition.and(cloth.user.id.eq(ownerId));
        if(typeEqual != null) {
            condition.and(cloth.clothType.eq(typeEqual));
        }

        if(cursor != null && idAfter !=null) {
            condition.and(cloth.createdAt.lt(cursor)
                .or(cloth.createdAt.eq(cursor).and(cloth.id.lt(idAfter))));
        }

        JPAQuery<Cloth> query = queryFactory.selectFrom(cloth);
        query.where(condition);
        query.orderBy(cloth.createdAt.desc(),cloth.id.desc());
        query.limit(limit+1);
        List<Cloth> clothsList = query.fetch();

        boolean hasNext=clothsList.size()>limit;
        if(hasNext) {
            clothsList.remove(clothsList.size()-1);
        }
        return new SliceImpl<>(clothsList, PageRequest.of(0, limit), hasNext);
    }

    @Override
    public Long getTotalCount(UUID ownerId, ClothType typeEqual) {
        QCloth cloth = QCloth.cloth;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(cloth.user.id.eq(ownerId));
        if(typeEqual != null) {
            builder.and(cloth.clothType.eq(typeEqual));
        }
        return queryFactory.select(cloth.count())
            .from(cloth)
            .where(builder)
            .fetchOne();
    }

}
