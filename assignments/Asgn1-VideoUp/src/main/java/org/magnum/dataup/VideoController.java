/*
 *
 * Copyright 2014 Jules White
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
 *
 */
package org.magnum.dataup;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;

@Controller
public class VideoController {

	private Map<Long, Video> videos;
	// an in-memory list that the servlet uses to
	// store the videos that are sent to it by clients
	private static final AtomicLong id = new AtomicLong(0L);

	@PostConstruct
	public void init() throws IOException {
		videos = new HashMap<Long, Video>();
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return videos.values();
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		if (v.getId() == 0)
			v.setId(id.incrementAndGet());
		v.setDataUrl(getDataUrl(v.getId()));
		videos.put(v.getId(), v);
		return v;
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.POST)
	public @ResponseBody VideoStatus addVideoData(
			@PathVariable(VideoSvcApi.ID_PARAMETER) long id,
			@RequestParam(VideoSvcApi.DATA_PARAMETER) MultipartFile videoData)
			throws Exception {
		if (!videos.containsKey(id))
			throw new ResourceNotFoundException("Invalid ID");
		VideoFileManager.get().saveVideoData(videos.get(id),
				videoData.getInputStream());
		return new VideoStatus(VideoStatus.VideoState.READY);
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.GET)
	public void getVideoData(@PathVariable(VideoSvcApi.ID_PARAMETER) long id,
			HttpServletResponse response) throws IOException {
		if (!videos.containsKey(id))
			throw new ResourceNotFoundException("Invalid id Provided");
		VideoFileManager.get().copyVideoData(videos.get(id),
				response.getOutputStream());
	}

	private String getDataUrl(long id) {
		return getUrlBaseFroLocalServer() + VideoSvcApi.VIDEO_SVC_PATH + "/"
				+ id + "/" + VideoSvcApi.DATA_PARAMETER;
	}

	private String getUrlBaseFroLocalServer() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes()).getRequest();
		return "http://"
				+ request.getServerName()
				+ ((request.getServerPort() != 80) ? ":"
						+ request.getServerPort() : "");
	}

}
