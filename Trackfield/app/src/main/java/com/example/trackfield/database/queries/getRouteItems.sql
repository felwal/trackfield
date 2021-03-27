
-- v3 (current)
select e.route_id, r.name, count(1) as amount, avg(e.effective_distance) as avg_distance, a.best_pace
from exercises as e
inner join routes as r on e.route_id = r._id
inner join (
	select e2.route_id, min(e2.time/e2.effective_distance)*1000 as best_pace
	from exercises as e2
	where e2.time > 0 and e2.effective_distance > 0 and (e2.type = 0)
	group by e2.route_id
	) as a on a.route_id = e.route_id
where r.hidden != 1 and (e.type = 0)
group by e.route_id
having count(1) > 1
order by amount desc

;

-- v2
select e.route_id, r.name, count(1) as amount, avg(e.effective_distance) as avg_distance,
	min(e.time/e.effective_distance)*1000 as best_pace
from exercises as e
inner join routes as r on e.route_id = r._id
where e.time > 0 and e.effective_distance > 0 and r.hidden != 1 and (e.type = 0 or e.type = 2)
group by e.route_id
having count(1) > 1
order by max(date) desc

;

-- v1
select e.route_id, r.name, antal, avg(e.distance) as avgDistance, case when varPaceDrv is null or pace < varPaceDrv then pace else varPaceDrv end minPace
from exercises as e inner join routes as r on e.route_id = r._id inner join
	(select route_id, count(1) as antal from exercises group by route_id having count(1) > 1) as a on e.route_id = a.route_id inner join
    (select route_id, min(time/distance)*1000 as pace from exercises where distance > 0 and time != 0 group by route_id) AS v on e.route_id = v.route_id left outer join
	(select e2.route_id, min(time/varDistAvg)*1000 as varPaceDrv from exercises as e2 inner join
		(select route_id, route_var, avg(distance) as varDistAvg from exercises where distance > 0 and time != 0 group by route_var, route_id) as vAvg on e2.route_id = vAvg.route_id and e2.route_var = vAvg.route_var
	where e2.distance = -1 and e2.time != 0 group by e2.route_var, e2.route_id) as vDrv on e.route_id = vDrv.route_id
where e.distance != -1 and r.hidden != 1 group by e.route_id order by max(date) desc
