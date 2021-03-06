package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelTexture;
import com.badlogic.gdx.graphics.g3d.utils.TextureProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public abstract class ModelLoader<P extends AssetLoaderParameters<Model>> extends AsynchronousAssetLoader<Model, P> {
	public ModelLoader (FileHandleResolver resolver) {
		super(resolver);
	}
	
	protected Array<ObjectMap.Entry<String, ModelData>> items = new Array<ObjectMap.Entry<String, ModelData>>(); 
	
	/** Directly load the raw model data on the calling thread. */ 
	public abstract ModelData loadModelData(final FileHandle fileHandle, P parameters);
	
	/** Directly load the raw model data on the calling thread. */ 
	public ModelData loadModelData(final FileHandle fileHandle) {
		return loadModelData(fileHandle, null);
	}
	
	/** Directly load the model on the calling thread. The model with not be managed by an {@link AssetManager}. */
	public Model loadModel(final FileHandle fileHandle, TextureProvider textureProvider, P parameters) {
		final ModelData data = loadModelData(fileHandle, parameters);
		return data == null ? null : new Model(data, textureProvider);
	}
	
	/** Directly load the model on the calling thread. The model with not be managed by an {@link AssetManager}. */
	public Model loadModel(final FileHandle fileHandle, P parameters) {
		return loadModel(fileHandle, new TextureProvider.FileTextureProvider(), parameters);
	}
	
	/** Directly load the model on the calling thread. The model with not be managed by an {@link AssetManager}. */
	public Model loadModel(final FileHandle fileHandle, TextureProvider textureProvider) {
		return loadModel(fileHandle, textureProvider, null);
	}
	
	/** Directly load the model on the calling thread. The model with not be managed by an {@link AssetManager}. */
	public Model loadModel(final FileHandle fileHandle) {
		return loadModel(fileHandle, new TextureProvider.FileTextureProvider(), null);
	}
	
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, P parameters) {
		final Array<AssetDescriptor> deps = new Array();
		ModelData data = loadModelData(resolve(fileName), parameters);
		if (data == null)
			return deps;
		
		ObjectMap.Entry<String, ModelData> item = new ObjectMap.Entry<String, ModelData>();
		item.key = fileName;
		item.value = data;
		synchronized(items) {
			items.add(item);
		}
		
		for (final ModelMaterial modelMaterial : data.materials) {
			if (modelMaterial.textures != null) {
				for (final ModelTexture modelTexture : modelMaterial.textures)
					deps.add(new AssetDescriptor(modelTexture.fileName, Texture.class));
			}
		}
		return deps;
	}
	
	@Override
	public void loadAsync (AssetManager manager, String fileName, P parameters) {
	}

	@Override
	public Model loadSync (AssetManager manager, String fileName, P parameters) {
		ModelData data = null;
		synchronized(items) {
			for (int i = 0; i < items.size; i++) {
				if (items.get(i).key.equals(fileName)) {
					data = items.get(i).value;
					items.removeIndex(i);
				}
			}
		}
		if (data == null)
			return null;
		final Model result = new Model(data, new TextureProvider.AssetTextureProvider(manager));
		data = null;
		return result;
	}
}
