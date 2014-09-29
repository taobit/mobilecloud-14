package org.magnum.mobilecloud.video;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

@Controller
public class VideoController {

	private final String ID = "id";
	private final String VIDEO_DATA_PATH = VideoSvcApi.VIDEO_SVC_PATH + "/{id}";
	private final String VIDEO_LIKE = VIDEO_DATA_PATH + "/like";
	private final String VIDEO_UNLIKE = VIDEO_DATA_PATH + "/unlike";
	private final String VIDEO_LIKEBY = VIDEO_DATA_PATH + "/likedby";
	private final String FIND_BY_NAME = VideoSvcApi.VIDEO_SVC_PATH +"/search/findByName";
	private final String LESS_THAN = VideoSvcApi.VIDEO_SVC_PATH +"/search/findByDurationLessThan";

	@Autowired
	private ApplicationContext context;
	private VideoRepository repository;

	@PostConstruct
	public void init() {
		repository = context.getBean(VideoRepository.class);
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		v.setLikes(0);
		return repository.save(v);
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return Lists.newArrayList(repository.findAll());
	}

	@RequestMapping(VIDEO_DATA_PATH)
	public @ResponseBody Video getVideoById(@PathVariable(ID) long id,
			HttpServletResponse response) {
		Video v = null;
		try {
			if (repository.exists(id))
				v = repository.findOne(id);
			if (v == null)
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} catch (IOException e) {
			e.printStackTrace();

		}
		return v;
	}

	@RequestMapping(value = VIDEO_LIKE, method = RequestMethod.POST)
	public void likeVideo(@PathVariable(ID) long id,
			HttpServletResponse response, Authentication authentication) {
		if (!repository.exists(id))
			try {
				response.sendError(HttpServletResponse.SC_NOT_FOUND,
						"Video Not Found!");
			} catch (IOException e) {
				e.printStackTrace();
			}
		else {
			Video video = repository.findOne(id);
			UserDetails user = (UserDetails) authentication.getPrincipal();
			if (video.getLikers().contains(user.getUsername()))
				try {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST,
							"You Have Liked It!");
				} catch (IOException e) {
					e.printStackTrace();
				}
			else {
				video.addLiker(user.getUsername());
//				video.increasLikes();
				repository.save(video);
			}
		}
	}

	@RequestMapping(value = VIDEO_UNLIKE, method = RequestMethod.POST)
	public void unlikeVideo(@PathVariable(ID) long id,
			HttpServletResponse response, Authentication authentication) {
		if (!repository.exists(id))
			try {
				response.sendError(HttpServletResponse.SC_NOT_FOUND,
						"Video Not Found");
			} catch (IOException e) {
				e.printStackTrace();
			}
		else {
			Video video = repository.findOne(id);
			UserDetails user = (UserDetails) authentication.getPrincipal();
			if (!video.getLikers().contains(user.getUsername()))
				try {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST,
							"You Have Not Liked It!");
				} catch (IOException e) {
					e.printStackTrace();
				}
			else {
				video.unLiker(user.getUsername());
				repository.save(video);
			}
		}
	}

	@RequestMapping(value = VIDEO_LIKEBY, method = RequestMethod.GET)
	public @ResponseBody Collection<String> getLikedBy(
			@PathVariable(ID) long id, HttpServletResponse response) {
		Set<String> likers = null;
		if (!repository.exists(id))
			try {
				response.sendError(HttpServletResponse.SC_NOT_FOUND,
						"Video Not Found");
			} catch (IOException e) {
				e.printStackTrace();
			}
		else {
			Video video = repository.findOne(id);
			likers = video.getLikers();
		}
		return likers;
	}

	@RequestMapping(value = FIND_BY_NAME, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> findByName(
			@RequestParam(VideoSvcApi.TITLE_PARAMETER) String title) {
		return repository.findByName(title);
	}

	@RequestMapping(value = LESS_THAN, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> findByDurationLessThan(
			@RequestParam(VideoSvcApi.DURATION_PARAMETER) String duration) {
		return repository.findByDurationLessThan(Long.parseLong(duration));
	}
}
