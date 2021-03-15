
select strftime('%d', date, 'unixepoch') as date_group,
sum(distance) as total_distance
from exercises
where date >= 1615161600 and date < 1615766400 --and (type = 0)--
group by date_group
order by date asc