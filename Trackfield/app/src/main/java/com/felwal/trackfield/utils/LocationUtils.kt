package com.felwal.trackfield.utils

import android.location.Location
import android.util.Log
import androidx.annotation.FloatRange
import com.google.android.gms.maps.model.LatLng
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

typealias Segment = List<LatLng>

// subsegment

fun Pair<Segment, Segment>.splitIntoSubSegments(): List<Pair<Segment, Segment>> {
    i(" ")
    i("new segment - - - - - - - - - – - - - - - -")
    i(" ")

    val subSegPairs = mutableListOf<Pair<Segment, Segment>>()

    val deltaDistMax = 25f
    val deltaSubSegRatioMax = 0.05f
    val stepDist = 8f
    val maxDist1 = first.distance()
    val maxDist2 = second.distance()

    // used to take longer steps when its longer between the points
    val splitStep: (Float) -> Float = y@{ deltaDist ->
        when {
            // add stepDist to be continious
            deltaDist <= deltaDistMax + stepDist -> stepDist
            // add stepDist and subtract deltaDistMax to be continiuos; then subtract stepDist count in the fact
            // that at best the next point is exactly stepDist meters closer than the last point.
            else -> deltaDist - deltaDistMax
        }
    }

        /*sinusoidalIncrease(
        xMin = splitDeltaDistMin, xMax = splitDeltaDistMax,
        yMin = splitStepDistMin, yMax = splitStepDistMax
    )*/

    var startDist1 = 0f
    var startDist2 = 0f
    var endDist1 = 0f
    var endDist2 = 0f

    var isSplitted = false

    while (endDist1 <= maxDist1 && endDist2 <= maxDist2) {
        val segPos1 = first.latlngAtDistance(endDist1, maxDist1)
        val segPos2 = second.latlngAtDistance(endDist2, maxDist2)
        val deltaDist = segPos1.distanceTo(segPos2)

        // we reached the end
        if (endDist1 >= maxDist1 - stepDist || endDist2 >= maxDist2 - stepDist) {
            // TODO: hur gör vi när sluten har olika längd / de slutar på olika ställen?
            val subSeg1 = first.subSegment(startDist1, maxDist1, maxDist1)
            val subSeg2 = second.subSegment(startDist2, maxDist2, maxDist2)
            subSegPairs.add(subSeg1 to subSeg2)

            break
        }

        // split; create new subsegment
        if (!isSplitted && deltaDist > deltaDistMax) {
            i("new splitted subsegment - - - - - - - - - – - - - - - -")

            // subtract half the stepDist to have the exact endDists as the medians of the distribution
            if (endDist1 - stepDist > startDist1 && endDist2 - stepDist > startDist2) {
                endDist1 -= stepDist / 2
                endDist2 -= stepDist / 2
            }

            val subSeg1 = first.subSegment(startDist1, endDist1, maxDist1)
            val subSeg2 = second.subSegment(startDist2, endDist2, maxDist2)

            /*val subSeg1Dist = subSeg1.distance()
            val subSeg2Dist = subSeg2.distance()
            val subSegDiff = abs(subSeg1Dist - subSeg2Dist)
            val subSegDiffRatio = subSegDiff / max(subSeg1Dist, subSeg2Dist)

            // too big difference in subsegment length makes the ratio loop inaccurate
            if (subSegDiffRatio > deltaSubSegRatioMax) {
                d("$subSeg1Dist, $subSeg2Dist; $subSegDiff => $subSegDiffRatio")
                val splitCount = (subSegDiffRatio / deltaSubSegRatioMax).roundToInt()
                val subSegSplits = subSeg1.split(splitCount) zip subSeg2.split(splitCount)
                subSegPairs.addAll(subSegSplits)
            }
            else*/ subSegPairs.add(subSeg1 to subSeg2)

            // the start of the next subsegment is the end of this one
            startDist1 = endDist1
            startDist2 = endDist2

            isSplitted = true
            continue
        }

        // look for end of subsegment
        if (isSplitted) {
            // keep first, iterate second
            var splitDist2 = endDist2
            while (splitDist2 >= startDist2) {

                val q = second.latlngAtDistance(splitDist2, maxDist2)
                val splitDeltaDist = segPos1.distanceTo(q)

                // merge; create new subsegment
                if (splitDeltaDist <= deltaDistMax) {
                    endDist2 = splitDist2

                    // subtract half the stepDist to have the exact endDists as the medians of the distribution
                    if (endDist1 - stepDist > startDist1 && endDist2 - stepDist > startDist2) {
                        endDist1 -= stepDist / 2
                        endDist2 -= stepDist / 2
                    }

                    val subSeg1 = first.subSegment(startDist1, endDist1, maxDist1)
                    val subSeg2 = second.subSegment(startDist2, endDist2, maxDist2)
                    subSegPairs.add(subSeg1 to subSeg2)

                    // the start of the next subsegment is the end of this one
                    startDist1 = endDist1
                    startDist2 = endDist2

                    isSplitted = false
                    break
                }

                splitDist2 -= splitStep(splitDeltaDist).also {
                    i(
                        "(2): dist1 = ${endDist1.toInt()}, " +
                            "dist2 = ${splitDist2.toInt()} ∈ [${startDist2.toInt()}, ${endDist2.toInt()}]; " +
                            "splitStep(${splitDeltaDist.toInt()}) = ${it.toInt()}"
                    )
                }
            }

            // dont iterate over first if we already found the mergepoint
            // by iterating over second
            if (!isSplitted) continue

            // keep second, iterate first
            // endDist1 - stepDist because endDist1 to endDist2 has already been compared in the loop above
            var splitDist1 = endDist1 - stepDist
            while (splitDist1 >= startDist1) {

                val p = first.latlngAtDistance(splitDist1, maxDist1)
                val splitDeltaDist = segPos2.distanceTo(p)

                // merge; create new subsegment
                if (splitDeltaDist <= deltaDistMax) {
                    endDist1 = splitDist1

                    // subtract half the stepDist to have the exact endDists as the medians of the distribution
                    if (endDist1 - stepDist > startDist1 && endDist2 - stepDist > startDist2) {
                        endDist1 -= stepDist / 2
                        endDist2 -= stepDist / 2
                    }

                    val subSeg1 = first.subSegment(startDist1, endDist1, maxDist1)
                    val subSeg2 = second.subSegment(startDist2, endDist2, maxDist2)
                    subSegPairs.add(subSeg1 to subSeg2)

                    // the start of the next subsegment is the end of this one
                    startDist1 = endDist1
                    startDist2 = endDist2

                    isSplitted = false
                    break
                }

                // take bigger steps if there is a long distance between the points
                splitDist1 -= splitStep(splitDeltaDist).also {
                    i(
                        "(1): dist2 = ${endDist2.toInt()}, " +
                            "dist1 = ${splitDist1.toInt()} ∈ [${startDist1.toInt()}, ${endDist1.toInt()}]; " +
                            "splitStep(${splitDeltaDist.toInt()}) = ${it.toInt()}"
                    )
                }
            }
        }

        endDist1 += stepDist
        endDist2 += stepDist
    }

    return subSegPairs
}

fun Segment.subSegment(startDistance: Float, endDistance: Float, segDistance: Float = distance()): Segment {
    if (startDistance > endDistance) throw IndexOutOfBoundsException("endDistance must be greater than startDistance")

    val subSegment = mutableListOf<LatLng>()
    var startIndex = -1
    var dist = 0f

    if (startDistance <= 0) {
        if (endDistance >= segDistance) return this
        startIndex = 0
    }

    for (i in 0 until size - 1) {
        val p = get(i)
        val q = get(i + 1)
        val distBetween = p.distanceTo(q)

        // look for start
        if (startIndex == -1 && dist + distBetween >= startDistance) {
            startIndex = i + 1
            subSegment.add(p.between(q, (startDistance - dist) / distBetween))

            // add averything from startDistance
            if (endDistance >= segDistance) {
                subSegment.addAll(subList(startIndex, size))
                break
            }
        }

        // look for end
        else if (dist + distBetween >= endDistance || i == size - 1) {
            subSegment.addAll(subList(startIndex, i))
            subSegment.add(p.between(q, (endDistance - dist) / distBetween))
            break
        }

        dist += distBetween
    }

    return subSegment
}

// at

fun Segment.latlngAtRatio(@FloatRange(from = 0.0, to = 1.0) ratio: Float): LatLng {
    if (size == 0) throw IndexOutOfBoundsException("Segment cannot be empty")
    if (size == 1 || ratio <= 0) return get(0)
    if (ratio >= 1) return get(size - 1)

    val segDist = distance()
    val distToRatio = (segDist * ratio)

    return latlngAtDistance(distToRatio, segDist)
}

fun Segment.latlngAtDistance(distance: Float, segDist: Float = distance()): LatLng {
    if (size == 0) throw IndexOutOfBoundsException("Segment cannot be empty")
    if (size == 1 || distance <= 0) return get(0)
    if (distance >= segDist) return get(size - 1)

    var dist = 0f

    for (i in 0 until size - 1) {
        val p = get(i)
        val q = get(i + 1)
        val distBetween = p.distanceTo(q)

        if (dist + distBetween >= distance || i == size - 2) {
            return p.between(q, (distance - dist) / distBetween)
        }
        else {
            dist += distBetween
        }
    }

    return get(size - 1)
}

// average

fun Segment.addValueToAverage(newValue: Segment, newValueIndex: Int): Segment {
    if (size == 0) return newValue

    val avgSegment = mutableListOf<LatLng>()
    val pointCount = max(distance(), newValue.distance()) / 15f
    val step = 1 / pointCount
    var ratio = 0f

    if (pointCount == 0f) {
        val newAvgAtRatio = get(0).addValueToAverage(newValue[0], newValueIndex)
        avgSegment.add(newAvgAtRatio)
        return avgSegment
    }

    while (ratio <= 1) {
        val avgAtRatio = latlngAtRatio(ratio)
        val newValueAtRatio = newValue.latlngAtRatio(ratio)
        val newAvgAtRatio = avgAtRatio.addValueToAverage(newValueAtRatio, newValueIndex)

        avgSegment.add(newAvgAtRatio)
        ratio += step
    }

    return avgSegment
}

fun LatLng.addValueToAverage(newValue: LatLng, newValueIndex: Int): LatLng {
    val newLat = latitude.addValueToAverage(newValue.latitude, newValueIndex)
    val newLng = longitude.addValueToAverage(newValue.longitude, newValueIndex)
    return LatLng(newLat, newLng)
}

fun List<Segment>.averageSegment(): Segment {
    if (size == 0) throw IndexOutOfBoundsException("Cannot average list of size 0")
    if (size == 1) return get(0)

    var avgSegment = get(0)

    // loop through segments
    for (i in 1 until size) {
        val avgSubSegments = mutableListOf<Segment>()

        val subSegPairs = (avgSegment to get(i)).splitIntoSubSegments()
        // loop through subsegments
        for ((avgSubSeg, newValueSubSeg) in subSegPairs) {
            val newAvgSubSeg = avgSubSeg.addValueToAverage(newValueSubSeg, i)
            avgSubSegments.add(newAvgSubSeg)
        }

        avgSegment = avgSubSegments.flatten()
    }

    return avgSegment
}

fun List<LatLng>.averageLatLng(): LatLng {
    if (size == 0) throw IndexOutOfBoundsException("Cannot average list of size 0")

    var latTot = 0.0
    var lngTot = 0.0

    for (latlng in this) {
        latTot += latlng.latitude
        lngTot += latlng.longitude
    }

    return LatLng(latTot / size, lngTot / size)
}

fun List<Segment>.averageSegmentSimple(): Segment {
    val avgSegment = mutableListOf<LatLng>()
    val pointCount = get(0).distance() / 15f
    val step = 1 / pointCount
    var ratio = 0f

    while (ratio <= 1 + step) {
        val coordsForAvg = mutableListOf<LatLng>()
        for (segment in this) {
            coordsForAvg.add(segment.latlngAtRatio(ratio))
        }
        avgSegment.add(coordsForAvg.averageLatLng())
        ratio += step
    }
    return avgSegment
}

//

fun Segment.distance(): Float {
    var distance = 0f

    for (i in 0 until size - 1) {
        val p: LatLng = get(i)
        val q: LatLng = get(i + 1)
        distance += p.distanceTo(q)
    }

    return distance
}

fun LatLng.distanceTo(other: LatLng): Float {
    val distBetweenArr = FloatArray(1)
    Location.distanceBetween(latitude, longitude, other.latitude, other.longitude, distBetweenArr)
    return distBetweenArr[0]
}

fun LatLng.between(other: LatLng, @FloatRange(from = 0.0, to = 1.0) ratio: Float): LatLng {
    val lat: Double = latitude + (other.latitude - latitude) * ratio
    val lng: Double = longitude + (other.longitude - longitude) * ratio

    return LatLng(lat, lng)
}
