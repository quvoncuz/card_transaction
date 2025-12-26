package card.uz.repository;

import card.uz.dto.FilterDTO;
import card.uz.dto.FilterResultDTO;
import card.uz.enums.TransactionType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CustomFilterRepository {

    @Autowired
    private EntityManager entityManager;

    public FilterResultDTO<Object[]> filter(FilterDTO dto, String cardId, int page, int size, TransactionType transactionType) {
        StringBuilder selectQuery = new StringBuilder("""
                select t.id,
                t.externalId,
                t.cardId,
                t.afterBalance,
                t.amount,
                t.currency,
                t.exchangeRate,
                t.purpose
                from TransactionEntity as t
                """);
        StringBuilder countQuery = new StringBuilder("""
                select count(t.id)
                from TransactionEntity as t
                """);
        StringBuilder whereClause = new StringBuilder(" where 1 = 1 ");

        Map<String, Object> params = new HashMap<>();

        if (dto.getExternalId() != null) {
            whereClause.append(" and t.externalId = :externalId ");
            params.put("externalId", dto.getExternalId());
        }
        if (dto.getCurrency() != null) {
            whereClause.append(" and t.currency = :currency ");
            params.put("currency", dto.getCurrency());
        }
        if (dto.getTransactionId() != null) {
            whereClause.append(" and t.id = :transactionId ");
            params.put("transactionId", dto.getTransactionId());
        }

        whereClause.append(" and t.cardId = :cardId ");
        params.put("cardId", cardId);
        whereClause.append(" and t.transactionType = :transactionType ");
        params.put("transactionType", transactionType);

        selectQuery.append(whereClause);
        Query selectQ = entityManager.createQuery(selectQuery.toString());
        params.forEach(selectQ::setParameter);

        selectQ.setFirstResult(page*size);
        selectQ.setMaxResults(size);
        List<Object[]> resultList = selectQ.getResultList();

        countQuery.append(whereClause);
        Query countQ = entityManager.createQuery(countQuery.toString());
        params.forEach(countQ::setParameter);

        Long totalCount = (Long) countQ.getSingleResult();

        return new FilterResultDTO<>(resultList, totalCount);
    }
}
