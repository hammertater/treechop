package ht.treechop.client.model;

import com.mojang.math.Vector3f;
import ht.tuber.math.Vector3;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;

public class ModelUtil {
    private static final ResourceLocation UNKNOWN_RESOURCE = new ResourceLocation("treechop", "dynamic");

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
        Vector3f posFrom3f = toVector3f(posFrom);
        Vector3f posTo3f = toVector3f(posTo);
        return switch (orientation) {
            case UP, DOWN -> new float[]{posFrom3f.x(), posFrom3f.z(), posTo3f.x(), posTo3f.z()};
            case EAST, WEST -> new float[]{posFrom3f.z(), posFrom3f.y(), posTo3f.z(), posTo3f.y()};
            case NORTH, SOUTH -> new float[]{posFrom3f.x(), posFrom3f.y(), posTo3f.x(), posTo3f.y()};
        };
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
                toVector3f(posFrom),
                toVector3f(posTo),
                new BlockElementFace(culling, -1, "", new BlockFaceUV(uvs, uvRotation)),
                sprite,
                orientation,
                BlockModelRotation.X0_Y0,
                null, // This is fine
                true,
                UNKNOWN_RESOURCE
        );
    }

    /**
     * Reverses {@link FaceBakery#fillVertex}
     */
    public static BakedQuad trimQuad(BakedQuad quad, Vector3 corner1, Vector3 corner2) {
        Vector3 mins = new Vector3(
                Math.min(corner1.x(), corner2.x()),
                Math.min(corner1.y(), corner2.y()),
                Math.min(corner1.z(), corner2.z())
        );

        Vector3 maxes = new Vector3(
                Math.max(corner1.x(), corner2.x()),
                Math.max(corner1.y(), corner2.y()),
                Math.max(corner1.z(), corner2.z())
        );

        int[] vertexData = trimQuadVertices(quad.getVertices(), mins, maxes);

        return new BakedQuad(
                vertexData,
                quad.getTintIndex(),
                quad.getDirection(),
                quad.getSprite(),
                quad.isShade()
        );
    }

    private static int[] trimQuadVertices(int[] vertexData, Vector3 mins, Vector3 maxes) {
        int vertexSize = vertexData.length / 4;

        Vertex oldV1 = getVertex(vertexData, 0, vertexSize);
        Vertex oldV2 = getVertex(vertexData, 1, vertexSize);
        Vertex oldV3 = getVertex(vertexData, 2, vertexSize);
        Vertex oldV4 = getVertex(vertexData, 3, vertexSize);

//        mins = new Vector3(0, 0, 0);
//        maxes = new Vector3(16, 16, 16);
        Vertex newV1 = lerpVertexUVsInTriangle(oldV1.xyz().clamp(mins, maxes), oldV1, oldV2, oldV3);
        Vertex newV2 = lerpVertexUVsInTriangle(oldV2.xyz().clamp(mins, maxes), oldV1, oldV2, oldV3);
        Vertex newV3 = lerpVertexUVsInTriangle(oldV3.xyz().clamp(mins, maxes), oldV1, oldV2, oldV3);
        Vertex newV4 = lerpVertexUVsInTriangle(oldV4.xyz().clamp(mins, maxes), oldV1, oldV2, oldV3);

        int[] newVertexData = Arrays.copyOf(vertexData, vertexData.length);
        setVertex(newVertexData, 0, vertexSize, newV1);
        setVertex(newVertexData, 1, vertexSize, newV2);
        setVertex(newVertexData, 2, vertexSize, newV3);
        setVertex(newVertexData, 3, vertexSize, newV4);

        return newVertexData;
    }

    private static Vertex lerpVertexUVsInTriangle(Vector3 pos, Vertex v1, Vertex v2, Vertex v3) {
        Vector3 v1Weights = getLerpWeightsInSimplex(pos, v1.xyz(), v2.xyz(), v3.xyz());
        double u = v1Weights.dot(v1.u, v2.u, v3.u);
        double v = v1Weights.dot(v1.v, v2.v, v3.v);
        return new Vertex(pos.x, pos.y, pos.z, u, v);
    }

    private static Vector3 getLerpWeightsInSimplex(Vector3 pos, Vector3 v1, Vector3 v2, Vector3 v3) {
        // Get the plane that the triangle (v1, v2, v3) lies on
        Vector3 side1 = v2.subtract(v1);
        Vector3 side2 = v3.subtract(v1);
        Vector3 normal = (side2).cross(side1).normalize();

        // Project p to the plane
        double offset = pos.subtract(v1).dot(normal);
        Vector3 p = pos.subtract(normal.scale(offset));

        // Find weights that lerp v1, v2, v3 to equal p
        // See https://answers.unity.com/questions/383804/calculate-uv-coordinates-of-3d-point-on-plane-of-m.html
        Vector3 f1 = v1.subtract(p);
        Vector3 f2 = v2.subtract(p);
        Vector3 f3 = v3.subtract(p);

        Vector3 va = side1.cross(side2);
        Vector3 va1 = f2.cross(f3);
        Vector3 va2 = f3.cross(f1);
        Vector3 va3 = f1.cross(f2);

        double a = va.length();
        double w1 = va1.length() / a * Math.signum(va.dot(va1));
        double w2 = va2.length() / a * Math.signum(va.dot(va2));
        double w3 = va3.length() / a * Math.signum(va.dot(va3));

        return new Vector3(w1, w2, w3);
    }

    private static Vertex getVertex(int[] vertexData, int index, int vertexSize) {
        int i = index * vertexSize;

        float x = Float.intBitsToFloat(vertexData[i]) * 16;
        float y = Float.intBitsToFloat(vertexData[i + 1]) * 16;
        float z = Float.intBitsToFloat(vertexData[i + 2]) * 16;
        // float w = Float.intBitsToFloat(vertexData[i + 3]);
        float u = Float.intBitsToFloat(vertexData[i + 4]);
        float v = Float.intBitsToFloat(vertexData[i + 5]);

        return new Vertex(x, y, z, u, v);
    }

    private static void setVertex(int[] vertexData, int index, int vertexSize, Vertex vertex) {
        int i = index * vertexSize;

        vertexData[i] = Float.floatToIntBits((float) vertex.x  / 16f);
        vertexData[i + 1] = Float.floatToIntBits((float) vertex.y / 16f);
        vertexData[i + 2] = Float.floatToIntBits((float) vertex.z / 16f);
        // vertexData[i + 3] Not important
        vertexData[i + 4] = Float.floatToIntBits((float) vertex.u);
        vertexData[i + 5] = Float.floatToIntBits((float) vertex.v);
    }

    public static BakedQuad translateQuad(BakedQuad quad, Vector3 translation) {
        int[] vertexData = Arrays.copyOf(quad.getVertices(), quad.getVertices().length);
        translateVertex(vertexData, 0, translation);
        translateVertex(vertexData, 1, translation);
        translateVertex(vertexData, 2, translation);
        translateVertex(vertexData, 3, translation);

        return new BakedQuad(vertexData, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
    }

    private static void translateVertex(int[] vertexData, int index, Vector3 translation) {
        int vertexSize = vertexData.length / 4;
        Vertex vertex = getVertex(vertexData, index, vertexSize);
        setVertex(vertexData, index, vertexSize, new Vertex(
                vertex.x + translation.x,
                vertex.y + translation.y,
                vertex.z + translation.z,
                vertex.u,
                vertex.v
        ));
    }

    private record Vertex(double x, double y, double z, double u, double v) {
        public Vector3 xyz() {
            return new Vector3(x, y, z);
        }
    }

    private static Vector3f toVector3f(Vector3 vec) {
        return new Vector3f((float)vec.x, (float)vec.y, (float)vec.z);
    }
}
