/*
 * Copyright (c) 2016. See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mbrlabs.mundus.editor.ui.modules.dialogs.importer;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.mbrlabs.mundus.editor.assets.AssetAlreadyExistsException;
import com.mbrlabs.mundus.editor.assets.EditorAssetManager;
import com.mbrlabs.mundus.commons.assets.Asset;
import com.mbrlabs.mundus.editor.core.Inject;
import com.mbrlabs.mundus.editor.core.Mundus;
import com.mbrlabs.mundus.editor.core.project.ProjectManager;
import com.mbrlabs.mundus.editor.core.registry.Registry;
import com.mbrlabs.mundus.editor.events.AssetImportEvent;
import com.mbrlabs.mundus.editor.ui.Ui;
import com.mbrlabs.mundus.editor.ui.modules.dialogs.BaseDialog;
import com.mbrlabs.mundus.editor.ui.widgets.ImageChooserField;
import com.mbrlabs.mundus.editor.utils.FileFormatUtils;
import com.mbrlabs.mundus.editor.utils.Log;

import java.io.IOException;

/**
 * @author Marcus Brummer
 * @version 07-06-2016
 */
public class ImportTextureDialog extends BaseDialog implements Disposable {

    private static final String TAG = ImportTextureDialog.class.getSimpleName();

    private ImportTextureTable importTextureTable;

    @Inject
    private Registry registry;
    @Inject
    private ProjectManager projectManager;

    public ImportTextureDialog() {
        super("Import Texture");
        Mundus.inject(this);
        setModal(true);
        setMovable(true);

        Table root = new VisTable();
        add(root).expand().fill();
        importTextureTable = new ImportTextureTable();

        root.add(importTextureTable).minWidth(300).expand().fill().left().top();
    }

    @Override
    public void dispose() {
        importTextureTable.dispose();
    }

    @Override
    protected void close() {
        super.close();
        importTextureTable.removeTexture();
    }

    /**
     *
     */
    private class ImportTextureTable extends VisTable implements Disposable {
        // UI elements
        private VisTextButton importBtn = new VisTextButton("IMPORT");
        private ImageChooserField imageChooserField = new ImageChooserField(300);

        public ImportTextureTable() {
            super();
            this.setupUI();
            this.setupListener();

            align(Align.topLeft);
        }

        private void setupUI() {
            padTop(6).padRight(6).padBottom(22);
            add(imageChooserField).grow().row();
            add(importBtn).grow().row();
        }

        private void setupListener() {
            importBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    try {
                        FileHandle texture = imageChooserField.getFile();
                        if (texture != null && texture.exists() && FileFormatUtils.isImage(texture)) {
                            EditorAssetManager assetManager = projectManager.current().assetManager;
                            Asset asset = assetManager.createTextureAsset(texture);
                            Mundus.postEvent(new AssetImportEvent(asset));
                            close();
                            Ui.getInstance().getToaster().success("Texture imported");
                        } else {
                            Ui.getInstance().getToaster().error("There is nothing to import");
                        }
                    } catch (IOException e) {
                        Log.exception(TAG, e);
                        Ui.getInstance().getToaster().error("IO error");
                    } catch (AssetAlreadyExistsException ee) {
                        Log.exception(TAG, ee);
                        Ui.getInstance().getToaster().error("Error: There already exists a texture with the same name");
                    }
                }
            });
        }

        public void removeTexture() {
            imageChooserField.removeImage();
        }

        @Override
        public void dispose() {

        }
    }

}