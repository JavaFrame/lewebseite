package ninja.seppli.lewebseite.common.media.pipe;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import ninja.seppli.lewebseite.common.exception.MediaEditException;
import ninja.seppli.lewebseite.common.media.Media;
import ninja.seppli.lewebseite.common.media.MediaService;
import ninja.seppli.lewebseite.common.settings.CommonSettings;

/**
 * The pipe workers asyncly edits media files
 * @author Sebastian Zumbrunn
 * @version 1.0
 *
 */
@Service
public class PipeWorker {
	/**
	 * logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * the executor service
	 */
	private ExecutorService service;
	/**
	 * the media service
	 */
	private MediaService mediaService;

	/**
	 * the applicatino context to instante new beans
	 */
	private ApplicationContext appContext;

	/**
	 * a map with all status
	 */
	private Map<Long, PipeStatus> statusMap = new HashMap<>();

	/**
	 * Constructor
	 * @param settings the common settings
	 * @param mediaService
	 * @param appContext
	 */
	@Autowired
	public PipeWorker(CommonSettings settings, MediaService mediaService, ApplicationContext appContext) {
		service = Executors.newFixedThreadPool(settings.getPipeThreads());
		this.mediaService = mediaService;
		this.appContext = appContext;
	}

	/**
	 * Puts the job in to the job list
	 * @param job the job
	 * @param file the temp file, which is the media
	 * @return the status
	 */
	public void acceptJob(PipeJob<?> job, File file) {
		Media media =  mediaService.save(job.getMedia());
		service.execute(createJobRunnable(job, file, media));
	}

	/**
	 * Creates a runnable for the given pipe job
	 * @param <T> the media type
	 * @param job the pipe job
	 * @param tempFile the media temp file
	 * @param media the media object
	 * @param pipeStatus the pipe status
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T extends Media> Runnable createJobRunnable(PipeJob<T> job, File tempFile, Media media) {
		return () -> {
			Media workingMedia = media;
			MediaService mediaService = appContext.getBean(MediaService.class);
			@SuppressWarnings("unchecked")
			PipeStatus status = new PipeStatus(workingMedia.getId());
			statusMap.put(status.getMediaId(), status);

			PipeFunction<T> pipeFunction  = null;
			if(job.getPipeFunction() != null) {
				pipeFunction = job.getPipeFunction();
			} else {
				pipeFunction = appContext.getBean(job.getPipeFunctionClass());
			}
			try {
				workingMedia = pipeFunction.edit((T) workingMedia, tempFile, status);
			} catch (MediaEditException e) {
				logger.error("An error occured while editing the media \"{}\" with the function \"{}\"", workingMedia.getClass().getName(), pipeFunction.getClass().getName(), e);

			}

			workingMedia.setProcessed(true);
			mediaService.save(workingMedia);
			try {
				mediaService.createFileAndWrite(workingMedia, new FileInputStream(tempFile));
				if(!tempFile.delete()) {
					logger.warn("Couldn't delete temp file \"{}\"");
				}
				logger.info("Processed \"{}\" (id: {})", workingMedia.getFileName(), workingMedia.getId());
			} catch (IOException e) {
				logger.error("Coulnd't read tempfile \"{}\"", tempFile.getAbsolutePath());
			}
		};
	}

	/**
	 * Returns the pipe status or null if the id doesn't exist
	 * @param mediaId the id of the media
	 * @return the pipe status or null
	 */
	public PipeStatus getPipeStatus(long mediaId) {
		return statusMap.get(mediaId);
	}
}
