/* MyMAM - Open Source Digital Media Asset Management.
 * http://www.mymam.net
 *
 * Copyright 2013, MyMAM contributors as indicated by the @author tag.
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

package net.mymam.upload;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.*;

/**
 * Wrap multipart request in order to make uploaded files accessible
 * to the {@link net.mymam.ui.Upload Upload} component.

 * <p/>
 * See section "How do I support file uploads" in <i>Core Java Server Faces</i>, 3rd Edition.

 * <p/>
 * TODO: Maybe replace with BalusC's servlet 3.0-based implementation,
 * http://balusc.blogspot.de/2009/12/uploading-files-in-servlet-30.html
 *
 * @author fstab
 */
public class UploadMultipartRequestWrapper extends HttpServletRequestWrapper {

    private Map<String, List<String>> formParameters = new HashMap<>();
    private Map<String, List<FileItem>> fileParameters = new HashMap<>();

    public UploadMultipartRequestWrapper(HttpServletRequest request, ServletFileUpload upload) throws ServletException {
        super(request);
        try {
            List fileItems = upload.parseRequest(request);
            for (int i = 0; i < fileItems.size(); i++) {
                FileItem item = (FileItem) fileItems.get(i);
                if (item.isFormField()) {
                    addParameter(item.getFieldName(), item.getString(), formParameters);
                } else {
                    addParameter(item.getFieldName(), item, fileParameters);
                }
            }
        } catch (FileUploadException e) {
            // Request timed out
            ServletException servletException = new ServletException();
            servletException.initCause(e);
            throw servletException;
        }
    }

    // Add FileItems to fileParameters, and Strings to formParameters.
    private <T> void addParameter(String key, T value, Map<String, List<T>> map) {
        if (map.containsKey(key)) {
            map.get(key).add(value);
        } else {
            List<T> values = new ArrayList<T>();
            values.add(value);
            map.put(key, values);
        }
    }

    @Override
    public String getParameter(String name) {
        if (formParameters.containsKey(name)) {
            List<String> values = formParameters.get(name);
            if (values.isEmpty()) {
                return "";
            }
            else {
                return values.get(0);
            }
        } else {
            return super.getParameter(name);
        }
    }

    @Override
    public Map getParameterMap() {
        Map<String, String[]> map = new HashMap<>();
        for (String formParam : formParameters.keySet()) {
            map.put(formParam, formParameters.get(formParam).toArray(new String[] {}));
        }
        map.putAll(super.getParameterMap());
        return Collections.unmodifiableMap(map);
    }

    @Override
    public Enumeration getParameterNames() {
        Set<String> paramNames = new LinkedHashSet<String>();
        paramNames.addAll(formParameters.keySet());
        Enumeration<String> original = super.getParameterNames();
        while (original.hasMoreElements()) {
            paramNames.add(original.nextElement());
        }
        return Collections.enumeration(paramNames);
    }

    @Override
    public String[] getParameterValues(String name) {
        if (formParameters.containsKey(name)) {
            List<String> values = formParameters.get(name);
            if (values.isEmpty()) {
                return new String[] {};
            }
            else {
                return values.toArray(new String[values.size()]);
            }
        } else {
            return super.getParameterValues(name);
        }
    }

    /**
     * Get the file parameter from a multipart request.
     *
     * @param name the {@link net.mymam.ui.Upload Upload} component's client id.
     * @return the {@link FileItem} representing the file in the request.
     */
    public FileItem getFileItem(String name) {
        if (fileParameters.containsKey(name)) {
            List<FileItem> items = fileParameters.get(name);
            // We assume that each request has only one file item.
            // This is configurable in jQuery's file upload plugin with 'singleFileUploads=true'.
            // One file per request is the default configuration in 'jquery.fileupload.js'.
            if ( items.size() != 1 ) {
                throw new UnsupportedOperationException("Cannot handle multiple files in a single request.");
            }
            return items.get(0);
        } else {
            return null;
        }
    }
}
