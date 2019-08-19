package com.web.controller;

import java.io.File;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.web.communication.SegmentationRequest;
import com.web.config.LarexConfiguration;
import com.web.facade.segmentation.LarexFacade;
import com.web.facade.segmentation.SegmentationSettings;
import com.web.io.FileDatabase;
import com.web.io.FilePathManager;
import com.web.model.PageAnnotations;

/**
 * Communication Controller to handle requests for the main viewer/editor.
 * Handles requests about displaying book scans and segmentations.
 * 
 */
@Controller
@Scope("request")
public class SegmentationController {
	@Autowired
	private ServletContext servletContext;
	@Autowired
	private FilePathManager fileManager;
	@Autowired
	private LarexConfiguration config;

	/**
	 * Initialize the controller by loading the fileManager and settings if not
	 * loaded already.
	 **/
	@PostConstruct
	private void init() {
		if (!fileManager.isInit()) {
			fileManager.init(servletContext);
		}
		if (!config.isInitiated()) {
			config.read(new File(fileManager.getConfigurationFile()));
			String bookFolder = config.getSetting("bookpath");
			if (!bookFolder.equals("")) {
				fileManager.setLocalBooksPath(bookFolder);
			}
		}
	}

	@RequestMapping(value = "/segment", method = RequestMethod.POST, headers = "Accept=*/*", produces = "application/json", consumes = "application/json")
	public @ResponseBody PageAnnotations segment(@RequestBody SegmentationRequest segmentationRequest) {
		FileDatabase database = new FileDatabase(new File(fileManager.getLocalBooksPath()),
				config.getListSetting("imagefilter"));
		return LarexFacade.segmentPage(segmentationRequest.getSettings(), segmentationRequest.getPage(),
				segmentationRequest.isAllowToLoadLocal(), fileManager, database);
	}

	@RequestMapping(value = "/segmentation/settings", method = RequestMethod.POST)
	public @ResponseBody SegmentationSettings getBook(@RequestParam("bookid") int bookID) {
		FileDatabase database = new FileDatabase(new File(fileManager.getLocalBooksPath()),
				config.getListSetting("imagefilter"));

		return new SegmentationSettings(database.getBook(bookID));
	}
	@RequestMapping(value = "/emptysegment", method = RequestMethod.POST)
	public @ResponseBody PageAnnotations emptysegment(@RequestParam("bookid") int bookID,
			@RequestParam("pageid") int pageID) {
		FileDatabase database = new FileDatabase(new File(fileManager.getLocalBooksPath()),
				config.getListSetting("imagefilter"));
		return LarexFacade.emptySegmentPage(bookID, pageID, database);
	}

	@RequestMapping(value = "/segmentedpages", method = RequestMethod.POST)
	public @ResponseBody Collection<Integer> getOnServer(@RequestParam("bookid") int bookID) {
		FileDatabase database = new FileDatabase(new File(fileManager.getLocalBooksPath()),
				config.getListSetting("imagefilter"));
		return database.getSegmentedPageIDs(bookID);
	}
}
