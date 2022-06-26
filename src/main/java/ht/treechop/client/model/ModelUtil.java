package ht.treechop.client.model;

import ht.treechop.common.util.FaceShape;
import ht.treechop.common.util.Vector3;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.BlockFaceUV;
import net.minecraft.client.renderer.model.BlockPartFace;
import net.minecraft.client.renderer.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.SimpleModelTransform;

public class ModelUtil {
    public static BakedQuad makeQuad(
            TextureAtlasSprite sprite,
            FaceShape faceShape,
            Direction orientation,
            Direction culling
    ) {
        return makeQuad(
                sprite,
                faceShape.getCorner1(),
                faceShape.getCorner3(),
                orientation,
                culling
        );
    }

    public static BakedQuad makeQuad(
            TextureAtlasSprite sprite,
            Vector3 posFrom,
            Vector3 posTo,
            Direction orientation,
            Direction culling
    ) {
        return makeQuad(
                sprite,
                posFrom,
                posTo,
                orientation,
                culling,
                getUVsForQuad(posFrom, posTo, orientation),
                0
        );
    }

    private static float[] getUVsForQuad(Vector3 posFrom, Vector3 posTo, Direction orientation) {
        Vector3f posFrom3f = posFrom.asVector3f();
        Vector3f posTo3f = posTo.asVector3f();
        switch (orientation) {
            case UP:
            case DOWN:
                return new float[]{posFrom3f.x(), posFrom3f.z(), posTo3f.x(), posTo3f.z()};
            case EAST:
            case WEST:
                return new float[]{posFrom3f.z(), posFrom3f.y(), posTo3f.z(), posTo3f.y()};
            case NORTH:
            case SOUTH:
            default:
                return new float[]{posFrom3f.x(), posFrom3f.y(), posTo3f.x(), posTo3f.y()};
        }
    }

    public static BakedQuad makeQuad(
            TextureAtlasSprite sprite,
            Vector3 posFrom,
            Vector3 posTo,
            Direction orientation,
            Direction culling,
            float[] uvs,
            int uvRotation
    ) {
        return new FaceBakery().bakeQuad(
                posFrom.asVector3f(),
                posTo.asVector3f(),
                new BlockPartFace(culling, -1, "", new BlockFaceUV(uvs, uvRotation)),
                sprite,
                orientation,
                SimpleModelTransform.IDENTITY,
                null,
                true,
                null
        );
    }
}
