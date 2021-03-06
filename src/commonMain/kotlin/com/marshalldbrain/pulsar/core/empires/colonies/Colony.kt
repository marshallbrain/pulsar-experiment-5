package com.marshalldbrain.pulsar.core.empires.colonies

import com.marshalldbrain.pulsar.core.empires.colonies.construction.*
import com.marshalldbrain.pulsar.core.empires.colonies.districts.DistrictOverseer
import com.marshalldbrain.pulsar.core.empires.colonies.districts.DistrictType
import com.marshalldbrain.pulsar.core.resources.ResourceHelper
import com.marshalldbrain.pulsar.core.resources.ResourcePath
import com.marshalldbrain.pulsar.core.resources.ResourceType
import com.marshalldbrain.pulsar.core.universe.Body

//TODO add removal of tasks from construction with refund of non built units
class Colony(districts: Set<DistrictType>, body: Body) {

    private val properties = ColonyProperties(body)
    private val constructionManager = ConstructionManager()
    private val districtOverseer = DistrictOverseer(districts, properties)
    private val resourceHelper = ResourceHelper()

    val districts: Map<DistrictType, Int>
        get() = districtOverseer.districts
    val currentTasks: List<BuildTask>
        get() = constructionManager.currentTasks
    val buildQueue: List<BuildTask>
        get() = constructionManager.buildQueue
    val resourceModifiers: Map<ResourcePath, List<Float>>
        get() = resourceHelper.resourceModifiers
    val resourceAmounts: Map<ResourcePath, Int>
        get() = resourceHelper.resourceAmounts

    fun checkOrderPossible(target: Buildable, type: BuildType, amount: Int = 1, replace: Buildable? = null): Boolean {

        return when(target) {
            is DistrictType -> {
                districtOverseer.check(target, type, amount, replace as DistrictType?)
            }
            else -> false
        }

    }

    fun createTask(target: Buildable, type: BuildType, amount: Int = 1, replace: Buildable? = null) {

        if (checkOrderPossible(target, type, amount, replace)) {

            val task = when(target) {
                is DistrictType -> {
                    districtOverseer.createOrder(target, type, amount, replace as DistrictType?)
                }
                else -> throw UnsupportedOperationException("$target is not supported. " +
                        "This should also never be seen and is a bug if it is")
            }

            constructionManager.add(task)

        }

    }

    fun tickDay(timePassed: Int) {
        constructionManager.processTime(timePassed)
    }

    fun tickMonth() {
        val resourceDelta = districtOverseer.delta
        resourceHelper.add(resourceDelta)
    }

}