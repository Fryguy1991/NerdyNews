package com.chrisfry.nerdnews.business.exceptions

import java.lang.Exception

class LateArticleLoadException(message: String): Exception(message)