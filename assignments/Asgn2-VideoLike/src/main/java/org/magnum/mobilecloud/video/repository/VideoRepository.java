package org.magnum.mobilecloud.video.repository;

import java.util.Collection;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.magnum.mobilecloud.video.client.VideoSvcApi;

/**
 * An interface for a repository that can store Video objects and allow them to
 * be searched by title
 * 
 * @author tao
 *
 * @PepositoryRestResource annotation tells Spring Data Rest to expose the
 *                         VideoRepository trough a controller and map it to the
 *                         '/Video' path
 */
@RepositoryRestResource(path = VideoSvcApi.VIDEO_SVC_PATH)
public interface VideoRepository extends CrudRepository<Video, Long> {

	/**
	 * find all videos with a matching title
	 * 
	 * @param title
	 * @Param annotation tells Spring Data Rest which HTTP request parameter it
	 *        should use to fill in the 'title' variable used to search for
	 *        Videos
	 * @return
	 */
	public Collection<Video> findByName(
			@Param(VideoSvcApi.TITLE_PARAMETER) String title);

	/**
	 * find all videos that are shorter than a specified duration
	 * 
	 * @param duration
	 * @Param annotation tells Spring Rest which HTTP request parameter it
	 *        should use to fill in the 'duration' variable used to search or
	 *        Videos
	 * @return
	 */
	public Collection<Video> findByDurationLessThan(
			@Param(VideoSvcApi.DURATION_PARAMETER) long duration);
}
