/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.aws.ec2.services;

import static org.testng.Assert.assertNotNull;

import java.util.Set;

import org.jclouds.aws.ec2.AWSEC2ApiMetadata;
import org.jclouds.compute.internal.BaseComputeServiceContextLiveTest;
import org.jclouds.ec2.domain.Reservation;
import org.jclouds.ec2.domain.RunningInstance;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests behavior of {@code EC2Client}
 * 
 * @author Adrian Cole
 */
@Test(groups = "live", singleThreaded = true)
public class AWSInstanceClientLiveTest extends BaseComputeServiceContextLiveTest {
   public AWSInstanceClientLiveTest() {
      provider = "aws-ec2";
   }
   
   public static final String PREFIX = System.getProperty("user.name") + "-ec2";

   private AWSInstanceClient client;
   
   @Override
   @BeforeClass(groups = { "integration", "live" })
   public void setupContext() {
      super.setupContext();
      client = view.unwrap(AWSEC2ApiMetadata.CONTEXT_TOKEN).getApi().getInstanceServices();
   }

   @Test
   void testDescribeInstances() {
      for (String region : view.unwrap(AWSEC2ApiMetadata.CONTEXT_TOKEN).getApi().getAvailabilityZoneAndRegionServices().describeRegions().keySet()) {
         Set<? extends Reservation<? extends RunningInstance>> allResults = client.describeInstancesInRegion(region);
         assertNotNull(allResults);
         assert allResults.size() >= 0 : allResults.size();
      }
   }

}
