/**
 * Licensed to jclouds, Inc. (jclouds) under one or more
 * contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  jclouds licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jclouds.fujitsu.fgcp.services;

import java.util.concurrent.TimeUnit;

import org.jclouds.concurrent.Timeout;
import org.jclouds.fujitsu.fgcp.domain.VDisk;
import org.jclouds.fujitsu.fgcp.domain.VDiskStatus;

/**
 * API relating to additional storage.
 * 
 * @author Dies Koper
 */
@Timeout(duration = 60, timeUnit = TimeUnit.SECONDS)
public interface AdditionalDiskApi {

   VDiskStatus getStatus(String id);

   VDisk get(String id);

   void update(String id, String name, String value);

   void backup(String id);

   void restore(String systemId, String backupId);

   void destroy(String id);

   void detach(String diskId, String serverId);

   void destroyBackup(String sysId, String backupId);

   // Set<> listBackups(String sysId);
}
