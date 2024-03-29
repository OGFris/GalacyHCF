package galacy.galacyhcf.scoreboardapi.packet

import cn.nukkit.network.protocol.DataPacket
import galacy.galacyhcf.scoreboardapi.scoreboard.DisplayObjective

/**
 * @author CreeperFace
 */
data class SetDisplayObjectivePacket(val displayObjective: DisplayObjective) : DataPacket() {

    override fun pid() = NETWORK_ID

    override fun encode() {
        reset()
        val obj = displayObjective.objective

        putString(displayObjective.displaySlot.name.toLowerCase())
        putString(obj.name)
        putString(obj.displayName)
        putString(obj.criteria.name)
        putVarInt(displayObjective.sortOrder.ordinal)
    }

    override fun decode() {

    }

    companion object {
        const val NETWORK_ID = 0x6b.toByte()
    }
}