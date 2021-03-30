
select route from exercises
where (_id in (select _id from exercises where effective_distance >= 20000 and effective_distance <= 21000)
or _id in (select _id from exercises where effective_distance >= 20000 order by (time / effective_distance) asc limit 4))
order by (time / effective_distance)