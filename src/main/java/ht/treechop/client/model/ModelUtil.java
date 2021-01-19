package ht.treechop.client.model;

import ht.treechop.common.util.FaceShape;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.BlockFaceUV;
import net.minecraft.client.renderer.model.BlockPartFace;
import net.minecraft.client.renderer.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.SimpleModelTransform;

public class ModelUtil {
    public static BakedQuad makeQuad(
            ResourceLocation textureRL,
            TextureAtlasSprite sprite,
            FaceShape faceShape,
            Direction orientation,
            Direction culling
    ) {
        return makeQuad(
                textureRL,
                sprite,
                faceShape.getCorner1(),
                faceShape.getCorner3(),
                orientation,
                culling
        );
    }

    public static BakedQuad makeQuad(
            ResourceLocation textureRL,
            TextureAtlasSprite sprite,
            Vector3f posFrom,
            Vector3f posTo,
            Direction orientation,
            Direction culling
    ) {
        return makeQuad(
                textureRL,
                sprite,
                posFrom,
                posTo,
                orientation,
                culling,
                getUVsForQuad(posFrom, posTo, orientation),
                0
        );
    }

    private static float[] getUVsForQuad(Vector3f posFrom, Vector3f posTo, Direction orientation) {
        switch (orientation) {
            case UP:
            case DOWN:
                return new float[]{posFrom.getX(), posFrom.getZ(), posTo.getX(), posTo.getZ()};
            case EAST:
            case WEST:
                return new float[]{posFrom.getZ(), posFrom.getY(), posTo.getZ(), posTo.getY()};
            case NORTH:
            case SOUTH:
            default:
                return new float[]{posFrom.getX(), posFrom.getY(), posTo.getX(), posTo.getY()};
        }
    }

    public static BakedQuad makeQuad(
            ResourceLocation textureRL,
            TextureAtlasSprite sprite,
            Vector3f posFrom,
            Vector3f posTo,
            Direction orientation,
            Direction culling,
            float[] uvs,
            int uvRotation
    ) {
        return new FaceBakery().bakeQuad(
                posFrom,
                posTo,
                new BlockPartFace(culling, -1, textureRL.toString(), new BlockFaceUV(uvs, uvRotation)),
                sprite,
                orientation,
                SimpleModelTransform.IDENTITY,
                null,
                true,
                null
        );
    }
}
