
select interval, count(1) as amount
from exercises
where interval != ''
group by interval
having count(1) > 0
