/**
 * Web layer for Image module.
 * <p>
 * Contains REST controllers that expose image functionality to the web layer.
 * Controllers in this package handle HTTP requests and delegate to the image module's
 * public API interfaces.
 * <p>
 * Controllers:
 * <ul>
 * <li>{@code AdminImageController} - Admin image operations (upload, download, delete)</li>
 * <li>{@code UserImageController} - User image operations (generate, retrieve)</li>
 * <li>{@code PublicImageController} - Public image operations</li>
 * </ul>
 * 
 * <p>
 * Migration Notes:
 * - AdminImageController was moved from com.jotoai.voenix.shop.api.admin.images to align with Spring Modulith architecture
 * - Spring Modulith violation in UserImageController was fixed by exposing image generation through ImageFacade
 */
package com.jotoai.voenix.shop.image.web;