
select interval, count() as amount
from exercises
where interval != ''
group by interval
having count() > 0
