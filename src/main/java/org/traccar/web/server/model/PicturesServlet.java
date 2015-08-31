/*
 * Copyright 2015 Vitaly Litvak (vitavaque@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.web.server.model;

import com.google.gson.Gson;
import com.google.inject.persist.Transactional;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.traccar.web.shared.model.Picture;
import org.traccar.web.shared.model.PictureType;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class PicturesServlet  extends HttpServlet {
    @Inject
    private Provider<EntityManager> entityManager;
    @Inject
    protected Logger logger;

    @Transactional(rollbackOn = { IOException.class, RuntimeException.class })
    @RequireUser
    @RequireWrite
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PictureType pictureType;
        try {
            pictureType = PictureType.valueOf(req.getPathInfo().substring(1));
        } catch (IllegalArgumentException iae) {
            logger.log(Level.WARNING, "Incorrect picture type: " + req.getPathInfo(), iae);
            resp.getWriter().write("Unsupported picture type: " + req.getPathInfo());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        boolean allowSkippingPictures = "true".equals(req.getParameter("allowSkippingPictures"));

        ServletFileUpload servletFileUpload = new ServletFileUpload();

        Map<String, Picture> uploadedPictures = new HashMap<String, Picture>();
        OutputStream os = null;
        File file = null;
        try {
            FileItemIterator fileItemIterator = servletFileUpload.getItemIterator(req);
            while (fileItemIterator.hasNext()) {
                file = File.createTempFile("uploaded", ".image");
                file.deleteOnExit();
                os = new BufferedOutputStream(new FileOutputStream(file));
                FileItemStream next = fileItemIterator.next();
                IOUtils.copy(next.openStream(), os);
                os.flush();
                os.close();
                if (file.length() == 0) {
                    if (allowSkippingPictures) {
                        file.delete();
                        continue;
                    } else {
                        resp.getWriter().write(next.getFieldName() + ": File is empty.");
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        return;
                    }
                }

                if (file.length() > pictureType.getMaxFileSize()) {
                    resp.getWriter().write(next.getFieldName() + ": File is too big. Max size is " + pictureType.getMaxFileSize() + " bytes.");
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                ImageInputStream imageStream = ImageIO.createImageInputStream(file);
                Iterator<ImageReader> readers = ImageIO.getImageReaders(imageStream);
                if (readers == null || !readers.hasNext()) {
                    resp.getWriter().write(next.getFieldName() + ": This is not an image.");
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                ImageReader reader = readers.next();
                String mimeType = reader.getFormatName();
                ImageReadParam param = reader.getDefaultReadParam();
                reader.setInput(imageStream, true, true);

                BufferedImage image;
                try {
                    image = reader.read(0, param);
                } finally {
                    reader.dispose();
                    imageStream.close();
                }

                if (image.getWidth() > pictureType.getMaxWidth() || image.getHeight() > pictureType.getMaxHeight()) {
                    resp.getWriter().write(next.getFieldName() + ": Image dimesions are too big. Max dimensions are: " + pictureType.getMaxWidth() + "x" + pictureType.getMaxHeight() + ".");
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                Picture picture = new Picture();
                picture.setType(pictureType);
                picture.setWidth(image.getWidth());
                picture.setHeight(image.getHeight());
                picture.setMimeType(mimeType);
                picture.setData(FileUtils.readFileToByteArray(file));
                entityManager.get().persist(picture);

                uploadedPictures.put(next.getFieldName(), picture);

                file.delete();
            }
        } catch (FileUploadException fue) {
            logger.log(Level.WARNING, fue.getLocalizedMessage(), fue);
            throw new IOException(fue);
        } catch (IOException ioex) {
            logger.log(Level.WARNING, ioex.getLocalizedMessage(), ioex);
            throw ioex;
        } finally {
            IOUtils.closeQuietly(os);
            FileUtils.deleteQuietly(file);
        }

        Gson gson = GsonUtils.create();
        gson.toJson(uploadedPictures, resp.getWriter());
    }

    @Transactional(rollbackOn = { IOException.class, RuntimeException.class })
    @RequireUser
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long pictureId;
        try {
            pictureId = Long.parseLong(req.getPathInfo().substring(1));
        } catch (NumberFormatException nfe) {
            resp.getWriter().write("Incorrect picture id: " + req.getPathInfo().substring(1));
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Picture picture = entityManager.get().find(Picture.class, pictureId);
        if (picture == null) {
            resp.getWriter().write("Picture with id " + pictureId + " does not exist");
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        resp.setContentType("image/" + picture.getMimeType());
        resp.setContentLength(picture.getData().length);
        IOUtils.write(picture.getData(), resp.getOutputStream());
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
