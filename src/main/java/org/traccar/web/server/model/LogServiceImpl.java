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

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.persist.Transactional;
import org.traccar.web.client.model.LogService;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

@Singleton
public class LogServiceImpl extends RemoteServiceServlet implements LogService {
    @Transactional
    @RequireUser(roles = { Role.ADMIN })
    @Override
    public String getTrackerServerLog(short sizeKB) {
        return getLog("tracker-server.log", sizeKB);
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN })
    @Override
    public String getWrapperLog(short sizeKb) {
        return getLog("wrapper.log." + new SimpleDateFormat("yyyyMMdd").format(new Date()), sizeKb);
    }

    private String getLog(String fileName, short sizeKB) {
        File workingFolder = new File(System.getProperty("user.dir"));
        File logFile1 = new File(workingFolder, "logs" + File.separatorChar + fileName);
        File logFile2 = new File(workingFolder.getParentFile(), "logs" + File.separatorChar + fileName);
        File logFile3 = new File(workingFolder, fileName);

        File logFile = logFile1.exists() ? logFile1 :
                logFile2.exists() ? logFile2 :
                        logFile3.exists() ? logFile3 : null;

        if (logFile != null) {
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(logFile, "r");
                int length = 0;
                if (raf.length() > Integer.MAX_VALUE) {
                    length = Integer.MAX_VALUE;
                } else {
                    length = (int) raf.length();
                }
                /**
                 * Read last X kilobytes from file
                 */
                raf.seek(Math.max(0, raf.length() - sizeKB * 1024));
                byte[] result = new byte[Math.min(length, sizeKB * 1024)];
                raf.read(result);
                return new String(result);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    raf.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return ("Log is not available. Looked at " + logFile1.getAbsolutePath() +
                ", " + logFile2.getAbsolutePath() +
                ", " + logFile3.getAbsolutePath());
    }
}
