package ru.practicum.event.repository;


import org.springframework.stereotype.Repository;
import ru.practicum.event.controller.EventFilterParams;
import ru.practicum.event.model.Event;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@Repository
public class CustomEventRepositoryImpl implements CustomEventRepository{

    @PersistenceContext
    private EntityManager entityManager;

    public List<Event> findEventsByAdmin(EventFilterParams params) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> cq = cb.createQuery(Event.class);
        Root<Event> eventRoot = cq.from(Event.class);
        Predicate criteria = cb.conjunction();

        if (!params.getIds().isEmpty()) {
            criteria = cb.and(criteria, eventRoot.get("initiator").get("id").in(params.getIds()));
        }
        if (!params.getStates().isEmpty()) {
            criteria = cb.and(criteria, eventRoot.get("state").in(params.getStates()));
        }
        if(!params.getCategories().isEmpty()) {
            criteria = cb.and(criteria, eventRoot.get("category").in(params.getCategories()));
        }
        if(params.getRangeStart() != null) {
            criteria = cb.and(criteria, cb.greaterThanOrEqualTo(eventRoot.get("eventDate"),  params.getRangeStart()));
        }
        if(params.getRangeEnd() != null) {
            criteria = cb.and(criteria, cb.lessThanOrEqualTo(eventRoot.get("eventDate"),  params.getRangeEnd()));
        }
        cq.select(eventRoot).where(criteria);

        return entityManager.createQuery(cq)
                .setFirstResult(params.getFrom())
                .setMaxResults(params.getSize())
                .getResultList();
    }
}
