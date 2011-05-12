#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <stdlib.h>
#include <jpeglib.h>
#include <stdint.h>
#include <setjmp.h>


#define  LOG_TAG    "libbitmap"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

struct my_error_mgr {
	struct jpeg_error_mgr pub; /* "public" fields */

	jmp_buf setjmp_buffer;	/* for return to caller */
};

typedef struct my_error_mgr * my_error_ptr;

uint16_t *bitmapData;

METHODDEF(void)
my_error_exit (j_common_ptr cinfo)
{
	LOGE("ERROR EXIT!!");
	/* cinfo->err really points to a my_error_mgr struct, so coerce pointer */
	my_error_ptr myerr = (my_error_ptr) cinfo->err;
	char buffer[1024];

	/* Always display the message. */
	/* We could postpone this until after returning, if we chose. */
	(*cinfo->err->format_message) (cinfo, buffer);
	LOGE(buffer);

	/* Return control to the setjmp point */
	//longjmp(myerr->setjmp_buffer, 1);
}

static uint16_t  make565(int red, int green, int blue)
{
	return (uint16_t) ((red >> 3) << 11) | ((green >> 2) << 5) | ((blue >> 3));
}

static int read_JPEG(const char * filename, AndroidBitmapInfo*  info, void*  pixels) {

	struct jpeg_decompress_struct cinfo;
	struct my_error_mgr	jerr;

	FILE *infile;
	JSAMPARRAY buffer;
	int rowbytes, row;

	// Open the JPEG file from our local data storage
	if ((infile = fopen(filename, "rb")) == NULL) {
        LOGE("Could not open JPEG file %s\n!", filename);
		return 0;
	}

	cinfo.err = jpeg_std_error(&jerr.pub);
	jerr.pub.error_exit = my_error_exit;

	if (setjmp(jerr.setjmp_buffer))
	{
		/* If we get here, the JPEG code has found an error.
		 * We need to clean up the JPEG object, and return.
		 */
		fclose(infile);
		jpeg_destroy_decompress(&cinfo);
		LOGE("JPEG ERROR OR SOMETHING");
	}

	// Initialize the JPEG decompression
	jpeg_create_decompress(&cinfo);

	// Specify the data source, a file in our case
	jpeg_stdio_src(&cinfo, infile);


	if(jpeg_read_header(&cinfo, TRUE) != JPEG_HEADER_OK) {
		LOGE("HEADERS ARE BAD");
		return 0;
	}

	// Start the decompressor
	jpeg_start_decompress(&cinfo);

	rowbytes = cinfo.output_width * cinfo.output_components;

	// Make a one-row-high sample array that will go away when we are done with image
	buffer = (*cinfo.mem->alloc_sarray)
		((j_common_ptr) &cinfo, JPOOL_IMAGE, rowbytes, 1);

	bitmapData = (uint16_t*)calloc(info->height * info->width, sizeof(uint16_t));

	int i, r, g, b;

    uint16_t * bitmap_line = (uint16_t *)bitmapData;

	while(cinfo.output_scanline < cinfo.output_height) {
        uint16_t*  line = (uint16_t*)pixels;

		jpeg_read_scanlines(&cinfo, buffer, 1);

		for(i = 0; i < cinfo.output_width; i++) {
			r = buffer[0][i*3];
			g = buffer[0][i*3+1];
			b = buffer[0][i*3+2];
			line[i] = make565(r, g, b);
			bitmap_line[i] = make565(r, g, b);
		}
        pixels = (char*)pixels + info->stride;
        bitmap_line = bitmap_line + info->width;
	}

	// Release the JPEG decompressor
	jpeg_finish_decompress(&cinfo);

	// Destroy the decompression object
	jpeg_destroy_decompress(&cinfo);


	fclose(infile);
	return 0;
}

static void loadJPEG(AndroidBitmapInfo*  info, void*  pixels, int circleX, int circleY) {
	int  yy, xx, yStart, xStart, yEnd, xEnd;

	if (circleY - 100 > 0) {
		yStart = circleY - 100;
	} else {
		yStart = 0;
	}

	if (circleX - 100 > 0) {
		xStart = circleX - 100;
	} else {
		xStart = 0;
	}

	if (circleY + 100 < info->height) {
		yEnd = circleY + 100;
	} else {
		yEnd = info->height - 1;
	}

	if (circleX + 100 < info->width) {
		xEnd = circleX + 100;
	} else {
		xEnd = info->width - 1;
	}

	uint16_t* bitmap_line;
	bitmap_line = (uint16_t *)bitmapData + (yStart * info->width);
	pixels = (char*)pixels + (info->stride * yStart);

	boolean first = 1;
	uint16_t*  line;

	for (yy = yStart; yy <= yEnd; yy++) {
		uint16_t*  line = (uint16_t*)pixels;

        for (xx = xStart; xx <= xEnd; xx++) {
            line[xx] = bitmap_line[xx];
        }

        // go to next line
        pixels = (char*)pixels + info->stride;
        bitmap_line = bitmap_line + info->width;
	}
}

JNIEXPORT void JNICALL Java_edu_newpaltz_nynjmohonk_MapView_renderBitmap(JNIEnv * env, jobject  obj, jobject bitmap,  jlong  time_ms, jstring filename, jint circleX, jint circleY, jboolean first)
{
    AndroidBitmapInfo  info;
    void*              pixels;
    int                ret;
    
    
    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }
    
    if (info.format != ANDROID_BITMAP_FORMAT_RGB_565) {
        LOGE("Bitmap format is not RGB_565 !");
        return;
    }
    
    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

	const char *nativeFilename = (*env)->GetStringUTFChars(env, filename, 0);   


	/* Now fill the values with a nice little plasma */
    
	if(bitmapData == NULL || first) {
		read_JPEG(nativeFilename, &info, pixels);
	} else {
		if(circleX != -1 && circleY != -1) {
			loadJPEG(&info, pixels, circleX, circleY);
		}
	}
    AndroidBitmap_unlockPixels(env, bitmap);

}

