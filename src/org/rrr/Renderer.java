package org.rrr;

import org.newdawn.slick.opengl.Texture;
import org.rrr.entity.Entity;
import org.rrr.gui.Cursor;
import org.rrr.gui.Cursor.CursorAnimation;
import org.rrr.model.CTexModel;
import org.rrr.model.ColorModel;
import org.rrr.model.Loader;
import org.rrr.model.LwsAnimation;
import org.rrr.model.MapMesh;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Renderer {
	
	public float pWidth = RockRaidersRemake.WIDTH, pHeight = RockRaidersRemake.HEIGHT;
	
	private int uiVao;
	public void init(Loader l) {
		
		
		System.out.println("VAO BEFORE: " + uiVao);
		uiVao = l.getUiModel();
		System.out.println("VAO AFTER " + uiVao);
		
	}
	
	public void render(MapMesh mesh, Shader s) {
		
		glBindVertexArray(mesh.vao);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
		s.setUniVector3f("pos", mesh.pos);
		glActiveTexture(GL_TEXTURE0);
		int texIndex = 0;
		s.setUniBoolean("lines", false);
		
		for(int i = 0; i < mesh.height; i++) {
			for(int j = 0; j < mesh.width; j++) {
				texIndex = mesh.tex[j][i];
				glBindTexture(GL_TEXTURE_2D, mesh.texs.get(texIndex).getTextureID());
				glDrawElements(GL_TRIANGLES, 4*3, GL_UNSIGNED_INT, (i*mesh.width+j)*3*4*4);
			}
		}
//		s.setUniBoolean("lines", true);
//		for(int i = 0; i < mesh.height; i++)
//			glDrawElements(GL_LINE_STRIP, mesh.indCount/mesh.height, GL_UNSIGNED_INT, i*mesh.width*3*4*4);
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(2);
		glBindVertexArray(0);
		
	}
	
	public void render(ColorModel model) {
		
		glBindVertexArray(model.vao);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glDrawElements(GL_TRIANGLES, model.indCount, GL_UNSIGNED_INT, 0);
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindVertexArray(0);
		
	}
	
	public void render(ColorModel model, Texture tex, int frame) {
		
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, tex.getTextureID());
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		render(model);
		glBindTexture(GL_TEXTURE_2D, 0);
		
		
	}
	
	public void render(CTexModel cmodel, Shader s) {
		
		glActiveTexture(GL_TEXTURE0);
		
		glBindVertexArray(cmodel.vao);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
		
		s.setUniBoolean("calcAlpha", false);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glDepthMask(true);
		int ind = 0;
		for(int i = 0; i < cmodel.opaque.length; i++) {
			ind = cmodel.opaque[i];
			if(cmodel.doubleSided[ind]) {
				glDisable(GL_CULL_FACE);
			} else {
				glEnable(GL_CULL_FACE);
				glCullFace(GL_FRONT);
			}
			if(cmodel.texs[ind] != null) {
				glBindTexture(GL_TEXTURE_2D, cmodel.texs[ind][cmodel.texIndex%cmodel.texs[ind].length].getTextureID());
				if(cmodel.alpha[i] != null)
					s.setUniVector3f("aColor", cmodel.alpha[i]);
				else
					s.setUniVector3f("aColor", new Vector3f(1, -1, -1));
			} else {
				s.setUniVector3f("aColor", new Vector3f(-1));
			}
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glDrawElements(GL_TRIANGLES, cmodel.surfLen[ind], GL_UNSIGNED_INT, cmodel.surfStart[ind]*4);
		}
		
		s.setUniBoolean("calcAlpha", true);
		glDepthMask(false);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE);
		for(int i = 0; i < cmodel.translucent.length; i++) {
			ind = cmodel.translucent[i];
			if(cmodel.doubleSided[ind]) {
				glDisable(GL_CULL_FACE);
			} else {
				glEnable(GL_CULL_FACE);
				glCullFace(GL_FRONT);
			}
			if(cmodel.texs[ind] != null) {
				glBindTexture(GL_TEXTURE_2D, cmodel.texs[ind][cmodel.texIndex%cmodel.texs[ind].length].getTextureID());
				if(cmodel.alpha[i] != null)
					s.setUniVector3f("aColor", cmodel.alpha[i]);
				else
					s.setUniVector3f("aColor", new Vector3f(1, -1, -1));
			} else {
				s.setUniVector3f("aColor", new Vector3f(-1));
			}
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glDrawElements(GL_TRIANGLES, cmodel.surfLen[ind], GL_UNSIGNED_INT, cmodel.surfStart[ind]*4);
		}
		
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(2);
		glBindVertexArray(0);
		glBindTexture(GL_TEXTURE_2D, 0);
	}
	
	public void render(Entity e, Shader s) {
		
		Matrix4f temp = new Matrix4f(e.rot);
		temp._m30(temp.m30()+e.pos.x);
		temp._m31(temp.m31()+e.pos.y);
		temp._m32(temp.m32()+e.pos.z);
		s.setUniMatrix4f("modelTrans", temp);
		render(e.anims[e.currentAnimation], s);
		
	}
	
	public void render(LwsAnimation anim, Shader s) {
		
		for(int i = 0; i < anim.bd.lobjects; i++) {
			
			if(anim.bd.models[i] != null) {
				s.setUniMatrix4f("animTrans", anim.transforms[i]);
				s.setUniFloat("framealpha", anim.alpha[i]);
				anim.bd.models[i].texIndex = anim.frame;
				render(anim.bd.models[i], s);
			}
			
		}
		
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_CULL_FACE);
		glCullFace(GL_FRONT);
		
	}
	
	public void render(Cursor cursor, Shader s) {
		
		s.setUniMatrix4f("trans", new Matrix4f().identity());
		Vector2f scale = new Vector2f(transWidth(cursor.w), transHeight(cursor.h));
		s.setUniVector2f("scale", scale);
		Vector3f translate = new Vector3f(transWidth(cursor.x)-1f, 1f-transHeight(cursor.y), 0);
		s.setUniVector3f("translate", translate);
		glDisable(GL_CULL_FACE);
		
		glBindVertexArray(uiVao);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		if(cursor.curAnimation == -1) {
			cursor.base.bind();
			glBindTexture(GL_TEXTURE_2D, cursor.base.getTextureID());
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
		} else {
			CursorAnimation anim = cursor.animations[cursor.curAnimation];
			if(anim.usesBaseTex) {
				glBindTexture(GL_TEXTURE_2D, cursor.base.getTextureID());
				glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
			}
			
			glBindTexture(GL_TEXTURE_2D, anim.texs[anim.frame].getTextureID());
			
			scale.x = transWidth(anim.w);
			scale.y = transHeight(anim.h);
			translate.x = transWidth(cursor.x+anim.x)-1f;
			translate.y = 1f-transHeight(cursor.y+anim.y);
			s.setUniVector2f("scale", scale);
			s.setUniVector3f("translate", translate);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
		}
		
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindVertexArray(0);
		
		glEnable(GL_CULL_FACE);
		glCullFace(GL_FRONT);
	}
	
	public float transWidth(int x) {
		return x*2.0f/pWidth;
	}
	public float transHeight(int y) {
		return y*2.0f/pHeight;
	}

}
