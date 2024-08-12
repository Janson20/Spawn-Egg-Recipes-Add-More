package com.mojang.blaze3d.vertex;

import com.google.common.collect.Queues;
import com.mojang.math.MatrixUtil;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class PoseStack implements net.neoforged.neoforge.client.extensions.IPoseStackExtension {
    private final Deque<PoseStack.Pose> poseStack = Util.make(Queues.newArrayDeque(), p_85848_ -> {
        Matrix4f matrix4f = new Matrix4f();
        Matrix3f matrix3f = new Matrix3f();
        p_85848_.add(new PoseStack.Pose(matrix4f, matrix3f));
    });

    public void translate(double p_85838_, double p_85839_, double p_85840_) {
        this.translate((float)p_85838_, (float)p_85839_, (float)p_85840_);
    }

    public void translate(float p_254202_, float p_253782_, float p_254238_) {
        PoseStack.Pose posestack$pose = this.poseStack.getLast();
        posestack$pose.pose.translate(p_254202_, p_253782_, p_254238_);
    }

    public void scale(float p_85842_, float p_85843_, float p_85844_) {
        PoseStack.Pose posestack$pose = this.poseStack.getLast();
        posestack$pose.pose.scale(p_85842_, p_85843_, p_85844_);
        if (Math.abs(p_85842_) == Math.abs(p_85843_) && Math.abs(p_85843_) == Math.abs(p_85844_)) {
            if (p_85842_ < 0.0F || p_85843_ < 0.0F || p_85844_ < 0.0F) {
                posestack$pose.normal.scale(Math.signum(p_85842_), Math.signum(p_85843_), Math.signum(p_85844_));
            }
        } else {
            posestack$pose.normal.scale(1.0F / p_85842_, 1.0F / p_85843_, 1.0F / p_85844_);
            posestack$pose.trustedNormals = false;
        }
    }

    public void mulPose(Quaternionf p_254385_) {
        PoseStack.Pose posestack$pose = this.poseStack.getLast();
        posestack$pose.pose.rotate(p_254385_);
        posestack$pose.normal.rotate(p_254385_);
    }

    public void rotateAround(Quaternionf p_272904_, float p_273581_, float p_272655_, float p_273275_) {
        PoseStack.Pose posestack$pose = this.poseStack.getLast();
        posestack$pose.pose.rotateAround(p_272904_, p_273581_, p_272655_, p_273275_);
        posestack$pose.normal.rotate(p_272904_);
    }

    public void pushPose() {
        this.poseStack.addLast(new PoseStack.Pose(this.poseStack.getLast()));
    }

    public void popPose() {
        this.poseStack.removeLast();
    }

    public PoseStack.Pose last() {
        return this.poseStack.getLast();
    }

    public boolean clear() {
        return this.poseStack.size() == 1;
    }

    public void setIdentity() {
        PoseStack.Pose posestack$pose = this.poseStack.getLast();
        posestack$pose.pose.identity();
        posestack$pose.normal.identity();
        posestack$pose.trustedNormals = true;
    }

    public void mulPose(Matrix4f p_324519_) {
        PoseStack.Pose posestack$pose = this.poseStack.getLast();
        posestack$pose.pose.mul(p_324519_);
        if (!MatrixUtil.isPureTranslation(p_324519_)) {
            if (MatrixUtil.isOrthonormal(p_324519_)) {
                posestack$pose.normal.mul(new Matrix3f(p_324519_));
            } else {
                posestack$pose.computeNormalMatrix();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Pose {
        final Matrix4f pose;
        final Matrix3f normal;
        boolean trustedNormals = true;

        Pose(Matrix4f p_254509_, Matrix3f p_254348_) {
            this.pose = p_254509_;
            this.normal = p_254348_;
        }

        Pose(PoseStack.Pose p_324130_) {
            this.pose = new Matrix4f(p_324130_.pose);
            this.normal = new Matrix3f(p_324130_.normal);
            this.trustedNormals = p_324130_.trustedNormals;
        }

        void computeNormalMatrix() {
            this.normal.set(this.pose).invert().transpose();
            this.trustedNormals = false;
        }

        public Matrix4f pose() {
            return this.pose;
        }

        public Matrix3f normal() {
            return this.normal;
        }

        public Vector3f transformNormal(Vector3f p_324099_, Vector3f p_323689_) {
            return this.transformNormal(p_324099_.x, p_324099_.y, p_324099_.z, p_323689_);
        }

        public Vector3f transformNormal(float p_324226_, float p_324133_, float p_323766_, Vector3f p_324001_) {
            Vector3f vector3f = this.normal.transform(p_324226_, p_324133_, p_323766_, p_324001_);
            return this.trustedNormals ? vector3f : vector3f.normalize();
        }

        public PoseStack.Pose copy() {
            return new PoseStack.Pose(this);
        }
    }
}
