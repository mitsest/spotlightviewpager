# Spotlight ViewPager
A tutorial view with view pager functionality

![alt text](https://github.com/mitsest/spotlightviewpager/blob/master/device-2020-09-11-122350.gif "Library showcase")

To instantiate a SpotlightTutorialView with the default arguments in your activity

```kotlin

        val targetView: View = findViewById(R.id.target)
        val viewModel = SpotlightViewModel(
                "Lorem Ipsum",
                SubtitleModel("Subtitle text", 7),
                targetView)

        val targetView2: View = findViewById(R.id.target2)
        val viewModel2 = SpotlightViewModel(
                "Lorem Ipsum 2",
                SubtitleModel("Subtitle text", 9),
                targetView2)

        val targetView3: View = findViewById(R.id.target3)
        val viewModel3 = SpotlightViewModel(
                "Lorem Ipsum 3",
                SubtitleModel("Subtitle text", 13),
                targetView3)

        SpotlightView.addSpotlightView(context, listOf(viewModel, viewModel2, viewModel3))

```

There is also a second addSpotlightView method in case you want to create your own SpotlightView

```kotlin

public static SpotlightView addSpotlightView(@NonNull Activity activity, @Nullable SpotlightView spotlightView, @NonNull List<SpotlightViewModel> models) {

```

You can create one with Builder
```kotlin

        spotlightView = SpotlightView.Builder.getInstance(this)
                .setBackgroundOpacityAnimationDuration(800)
                .setTextOpacityAnimationDuration(300)
                .setSpotlightGrowAnimationDuration(300)
                .setSpotlightPulseAnimationDuration(1200)
                .setMoveAnimationDuration(750)
                .setCloseAnimationDuration(220)
                .setGrowRatio(0.7f)
                .build()
```

To use different colors you need to override the default library colors inside your own colors.xml
```xml
    <color name="spotlight_overlay_color">#EB171819</color>
    <color name="spotlight_text_color">#fff</color>
    <color name="spotlight_border_color">#9C27B0</color>
```
