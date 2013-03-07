package northwind.ui.example;

import reactive.ui.Auxiliary;
import reactive.ui.CannyTask;
import reactive.ui.Column;
import reactive.ui.ColumnDescription;
import reactive.ui.ColumnText;
import reactive.ui.DataGrid;
import reactive.ui.Decor;
import reactive.ui.Expect;
import reactive.ui.Layoutless;
import reactive.ui.RedactText;
import reactive.ui.SketchPlate;
import reactive.ui.TintBitmapTile;
import tee.binding.Bough;
import tee.binding.it.Note;
import tee.binding.it.Numeric;
import tee.binding.task.Task;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;

public class ActivityPromptEmployee extends Activity {
	Layoutless layoutless;
	Note what = new Note().afterChange(new CannyTask() {
		@Override
		public void doTask() {
			if (grid != null) {
				grid.clearColumns();
				requery();
				grid.refresh();
			}
		}
	}.laziness.is(200), true);
	ColumnDescription columnName = new ColumnDescription();
	ColumnDescription columnAddress = new ColumnDescription();

	DataGrid grid;
	Numeric offset = new Numeric().value(0);
	int pageSize = 25;
	Bough data;
	Expect refresh = new Expect().status.is("Wait...").task.is(new Task() {
		@Override
		public void doTask() {
			grid.clearColumns();
			requery();
		}
	})//
	.afterDone.is(new Task() {
		@Override
		public void doTask() {
			grid.refresh();
		}
	});

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		layoutless = new Layoutless(this);
		setContentView(layoutless);
		this.setTitle("Northwind UI Example - Employees");
		compose();
		refresh.start(this);
	}
	void compose() {
		layoutless.child(new Decor(this)//
				.background.is(Auxiliary.colorBackground)//
						.sketch(new SketchPlate().tint(new TintBitmapTile()//
								.bitmap.is(BitmapFactory.decodeResource(getResources(), R.drawable.snowflakes3))//
								)//
						.width.is(layoutless.width().property)//
						.height.is(layoutless.height().property)//
						)//
						.width().is(layoutless.width().property)//
						.height().is(layoutless.height().property)//
				);
		layoutless.child(new Decor(this)//
				.labelText.is("Employees")//
				.labelSize.is(0.2 * Auxiliary.screenWidth(this))//
				.labelColor.is(0xccffffff)//
						.width().is(layoutless.width().property)//
						.height().is(layoutless.height().property)//
				);
		int xx = (Auxiliary.screenWidth(this) - Auxiliary.tapSize * 5) / 2;
		if (xx < 0) {
			xx = 0;
		}
		layoutless.child(new Decor(this)//
				.bitmap.is(BitmapFactory.decodeResource(getResources(), R.drawable.zoom1))//
						.left().is(xx - 150)//
						.width().is(300)//
						.height().is(300)//
				);
		layoutless.child(new RedactText(this)//
				.text.is(what)//
						.left().is(xx)//
						.top().is(0.1 * Auxiliary.tapSize)//
						.width().is(Auxiliary.tapSize * 5)//
						.height().is(Auxiliary.tapSize * 0.8)//
				);
		grid = new DataGrid(this);
		layoutless.child(grid//
				.headerHeight.is(0.5 * Auxiliary.tapSize)//
				.pageSize.is(pageSize)//
				.dataOffset.is(offset)//
				.beforeFlip.is(new Task() {
					@Override
					public void doTask() {
						requery();
					}
				})//
				.center.is(true)//
						.columns(new Column[] { columnName.title.is("Name").width.is(4 * Auxiliary.tapSize)//
								, columnAddress.title.is("Address").width.is(4 * Auxiliary.tapSize) //
								})//
						.left().is(1)//
						.top().is(Auxiliary.tapSize)//
						.width().is(layoutless.width().property)//
						.height().is(layoutless.height().property.minus(Auxiliary.tapSize))//
				);
	}
	void tap(String id) {
		Intent intent = this.getIntent();
		intent.putExtra("customerID", id);
		this.setResult(RESULT_OK, intent);
		finish();
	}
	void requery() {
		String sql = "select * from Employees";
		String seek = what.value().trim().toUpperCase();
		sql = sql + "\n	where upper(Employees.FirstName) like '%" + seek + "%' or upper(Employees.LastName) like '%" + seek + "%'";
		sql = sql + "\n	order by Employees.LastName"//
				+ "\n limit " + (pageSize * 3) + " offset " + offset.value().intValue();
		data = Auxiliary.fromCursor(Tools.db(this).rawQuery(sql, null), true);
		for (int i = 0; i < data.children.size(); i++) {
			Bough row = data.children.get(i);
			final String id = row.child("EmployeeID").value.property.value();
			Task tap = new Task() {
				@Override
				public void doTask() {
					tap(id);
				}
			};
			columnName.cell(row.child("TitleOfCourtesy").value.property.value()+" "+row.child("FirstName").value.property.value()+" "+row.child("LastName").value.property.value(), tap,row.child("Title").value.property.value());
			columnAddress.cell(row.child("City").value.property.value() + ", " + row.child("Country").value.property.value(), tap, row.child("Address").value.property.value());
		}
	}
}
