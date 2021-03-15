
-- ver 4
select distances.distance, a.best_pace, a.best_time
from distances
left outer join (
    select distances.distance as distance,
    min(1000 * exercises.time / exercises.effective_distance) as best_pace,
    distances.distance * min(1000 * exercises.time / exercises.effective_distance) as best_time
    from exercises, distances
    where exercises.effective_distance >= distances.distance
        and exercises.type = 0 and exercises.time != 0
    group by distances.distance
    ) as a on distances.distance = a.distance

;

-- ver 5 (current)
select distance, (
    select min(1000 * time / effective_distance)
    from exercises
    where effective_distance >= distances.distance
        and type = 0 and time != 0
    ) as best_pace, best_pace * distance as best_time
from distances

;

select distance, (
    select min(1000 * time / effective_distance)
    from exercises
    where effective_distance >= distances.distance
        and type = 0 and time != 0
    ) as best_pace, (
    select distances.distance * min(1000 * time / effective_distance)
    from exercises
    where effective_distance >= distances.distance
        and type = 0 and time != 0
    ) as best_time
from distances

;
