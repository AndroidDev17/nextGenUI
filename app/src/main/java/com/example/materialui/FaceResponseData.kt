package com.example.materialui

import com.microsoft.projectoxford.face.contract.Face

data class FaceResponseData(
    val faces: Array<Face>?,
    val isSuccess: Boolean,
    val exception: Exception?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FaceResponseData

        if (faces != null) {
            if (other.faces == null) return false
            if (!faces.contentEquals(other.faces)) return false
        } else if (other.faces != null) return false
        if (isSuccess != other.isSuccess) return false

        return true
    }

    override fun hashCode(): Int {
        var result1 = faces?.contentHashCode() ?: 0
        result1 = 31 * result1 + isSuccess.hashCode()
        return result1
    }
}