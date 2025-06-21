package com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = CurseForgeFileReleaseType.Serializer::class)
enum class CurseForgeFileReleaseType(val code: Int) {
    RELEASE(1),
    BETA(2),
    ALPHA(3);

    companion object {
        private val map = entries.associateBy { it.code }
        fun fromCode(code: Int): CurseForgeFileReleaseType = map[code] ?: error("Unknown release code: $code")
    }

    object Serializer : KSerializer<CurseForgeFileReleaseType> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("CurseForgeFileReleaseType", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): CurseForgeFileReleaseType {
            val code = decoder.decodeInt()
            return CurseForgeFileReleaseType.fromCode(code)
        }

        override fun serialize(encoder: Encoder, value: CurseForgeFileReleaseType) {
            encoder.encodeInt(value.code)
        }
    }
}